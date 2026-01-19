package com.zhny.iot.ota.sdk.model;

import com.zhny.iot.ota.sdk.core.message.YModemFramePacket;
import com.zhny.iot.ota.sdk.core.message.YModemPacketType;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import static com.zhny.iot.ota.sdk.core.ExecutorPoolUtils.BUSINESS_EXECUTOR;

public abstract class ChannelDevice extends AbstractChannelDeviceBase {
    private final Logger  logger = LoggerFactory.getLogger (ChannelDevice.class);

    private YModemFramePacket executeMsg;
    protected Boolean isRun=false;
    private final Object sendLock = new Object();
    public ChannelDevice(Channel channel , String code) {
        super (channel,code);
    }
    private Integer tryCount = 0;
    public void onPass(){
        put(new YModemFramePacket((byte) YModemPacketType.PASS.getI()))
                .thenRun(()->logger.info("device IMEI [{}],send PASS message success",getKey()))
                .exceptionally(e->{
                    logger.error("device IMEI [{}],send PASS message error{}",getKey(),e);
                    return null;
                })
        ;
    }
    @Override
    public boolean equals(Object obj) {
        if(obj == null)
            return false;
        return this == obj || (obj instanceof ChannelDevice
                && Objects.equals(this.getKey(), ((ChannelDevice) obj).getKey())
                && this.getChannel ().id () == ((ChannelDevice)obj).getChannel ().id ());
    }

    boolean isEmpty(){
        synchronized (queueMessage){
            return queueMessage.isEmpty();
        }
    }
    boolean isFull(){
        synchronized (queueMessage){
            return queueMessage.size() >= 512;
        }
    }
    protected CompletableFuture<Void> put(YModemFramePacket message)  {
        CompletableFuture<Void> future = new CompletableFuture<>();
        if(message == null) {
            future.complete(null);
            return future;
        }

        boolean isNull = isEmpty();
        if(isFull()){
            future.completeExceptionally(new Exception("queue is full"));
            return future;
        }
        queueMessage.offer(message);
        if(isNull){
            if(!isRun){
                CompletableFuture.runAsync(()->{
                    loadCurrentMessage();
                    try {
                        onSend();
                        future.complete(null);
                    } catch (InterruptedException e) {
                        future.completeExceptionally(e);
                    }
                },BUSINESS_EXECUTOR);
            }
        } else {
            future.complete(null);
        }
        return future;
    }

    protected YModemFramePacket get(){
        return queueMessage.poll();
    }

    public void loadCurrentMessage(){
        if(!isEmpty()) {
            YModemFramePacket message=loadCommand();
            if(message != null)
                this.executeMsg=message;
        }else{
            this.executeMsg = null;
        }
        synchronized (isRun) {
            isRun= this.executeMsg != null;
        }
//        logger.info("channel[{}],queue isRun[{}],size[{}]",getKey(),isRun,queueMessage.size());
    }
    public abstract YModemFramePacket loadCommand();

    protected void onSend() throws InterruptedException {
        if(executeMsg != null){
            Channel ch = getChannel();
            if(ch.isActive()){
                logger.info("device[{}] send data[{}]",this.getKey(), executeMsg.toString());
                ch.writeAndFlush(executeMsg);
                Thread.sleep (1);
                sendNotify();
            }
        }
    }

    public void reviceMsgNotify(){
        synchronized(sendLock) {
            sendLock.notify();
        }
        tryCount = 0;
    }

    private void sendNotify() throws InterruptedException {
        long start = System.currentTimeMillis ();
        long timeout = 6000;
        synchronized(sendLock) {
            sendLock.wait(timeout);
        }
        long now = System.currentTimeMillis ();
        long timeSoFar = now - start;
        if (timeSoFar >= timeout){
            this.executeMsg.clear();
            onError(String.format("No response after exceeding the maximum number of retries %s", tryCount));
//            if(tryCount >= 0){
//                loadCurrentMessage();
//                this.executeMsg.clear();
//                tryCount = 0;
//                onError(String.format("No response after exceeding the maximum number of retries %s", tryCount));
//            }else{
//                tryCount ++ ;
//            }
//            this.onSend();
        }else{
            loadCurrentMessage();
            this.onSend();
        }
    }

    public abstract void onError(String msg);
}
