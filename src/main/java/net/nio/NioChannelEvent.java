package net.nio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class  NioChannelEvent implements ChannelEvent{
	private static final Logger LOG = LoggerFactory.getLogger(NioChannelEvent.class);
	private NioChannel channel ;
	private ChannelEventType type ;
	private NioHandler handler;
	private Object parameter;

	public   NioChannelEvent (NioChannel channel ,ChannelEventType type,NioHandler handler){
		this(channel, type, handler,null);
	}
	public   NioChannelEvent (NioChannel channel ,ChannelEventType type,NioHandler handler,Object parameter){
		this.channel = channel;
		this.type = type;
		this.handler = handler;
		this.parameter = parameter;
	}
	
	@Override
	public NioChannel getChannel() {
		return channel;
	}

	@Override
	public ChannelEventType getType() {
		return type;
	}

	@Override
	public void fire() {
		try {
			fire0();
		} catch (Exception e) {
			try {
				handler.channelThrown(channel, e);
			} catch (Exception ex) {
				LOG.info("Catch channel thrown exception", ex);
			}
		}
	}
	
	private void fire0()  {
		switch (type) {
		case CHANNEL_READ:
			handler.channelRead(channel, (byte[]) parameter);
			break;
		case CHANNEL_WRITTEN:
			handler.channelWritten(channel, (byte[]) parameter);
			break;
		case CHANNEL_THROWN:
			handler.channelThrown(channel, (Exception) parameter);
			break;
		case CHANNEL_OPENED:
			handler.channelOpened(channel);
			break;
		case CHANNEL_CLOSED:
			handler.channelClosed(channel);
			break;
		default:
			throw new IllegalArgumentException("Unknown event type: " + type);
		}
	}
	
  

}
