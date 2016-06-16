package net.nio;


public interface ChannelEvent {
	
    NioChannel getChannel();
    
    ChannelEventType getType();
    
    void fire();

}
