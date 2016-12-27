package com.chatt.demo.model;

import com.chatt.demo.UserList;

import java.util.Date;

/**
 * The Class Conversation is a Java Bean class that represents a single chat
 * conversation message.
 */
public class Conversation
{
	public  Conversation()
	{

	}

	/** The Constant STATUS_SENDING. */
	public static final int STATUS_SENDING = 0;

	/** The Constant STATUS_SENT. */
	public static final int STATUS_SENT = 1;

	/** The Constant STATUS_FAILED. */
	public static final int STATUS_FAILED = 2;

	/** The msg. */
	private String msg;

	/** The status. */
	private int status = STATUS_SENT;

	/** The date. */
	private Date date;

	/** The sender. */
	private String sender;

    /** The receiver */
    private String receiver;


	/**File*/
	private FileModel file;
	/**Map*/
	private  MapModel map;

	/**
	 * Instantiates a new conversation.
	 * 
	 * @param msg
	 *            the msg
	 * @param date
	 *            the date
	 * @param sender
	 *            the sender
     * @param receiver
     *            the receiver

	 */
	public Conversation(String msg, Date date, String sender, String receiver) {
        this.msg = msg;
		this.date = date;
		this.sender = sender;
        this.receiver = receiver;

	}
	public Conversation( Date date, String sender, String receiver, FileModel file) {

		this.date = date;
		this.sender = sender;
		this.receiver = receiver;
		this.file = file;
	}
	public Conversation( Date date, String sender, String receiver, MapModel map) {

		this.date = date;
		this.sender = sender;
		this.receiver = receiver;
		this.map = map;
	}

	/**
	 * gets the mapModel
	 * @return
     */
	public MapModel getMapModel() {
		return map;
	}

	/**
	 * sets the mapModel
	 * @param map
     */

	public void setMapModel(MapModel map) {
		this.map = map;
	}


	/**
	 * gets the filemodel
	 * @return
     */
	public FileModel getFile() {
	return file;
}

	/**
	 * sets the filemodel
	 * @param file
     */

	public void setFile(FileModel file) {
		this.file = file;
	}


	/**
	 * Gets the msg.
	 * 
	 * @return the msg
	 */
	public String getMsg()
	{
		return msg;
	}

	/**
	 * Sets the msg.
	 *
	 * @param msg
	 *            the new msg
	 */
	public void setMsg(String msg)
	{
		this.msg = msg;
	}

	/**
	 * Checks if is sent.
	 * 
	 * @return true, if is sent
	 */
	public boolean isSent()
	{
		return UserList.user.getId().contentEquals(sender);
	}

	/**
	 * Gets the date.`
	 * 
	 * @return the date
	 */
	public Date getDate() {

        return date;
	}

	/**
	 * Sets the date.
	 *
	 * @param date
	 *            the new date
	 */
	public void setDate(Date date)
	{
		this.date = date;
	}

    /**
     * Gets the sender.
     *
     * @return the sender
     */
    public String getReceiver()
    {
        return receiver;
    }

    /**
     * Sets the sender.
     *
     * @param receiver
     *            the new sender
     */
    public void setReceiver(String receiver)
    {
        this.receiver = receiver;
    }

	/**
	 * Gets the sender.
	 * 
	 * @return the sender
	 */
	public String getSender()
	{
		return sender;
	}

	/**
	 * Sets the sender.
	 *
	 * @param sender
	 *            the new sender
	 */
	public void setSender(String sender)
	{
		this.sender = sender;
	}

	/**
	 * Gets the status.
	 * 
	 * @return the status
	 */
	public int getStatus()
	{
		return status;
	}

	/**
	 * Sets the status.
	 * 
	 * @param status
	 *            the new status
	 */
	public void setStatus(int status)
	{
		this.status = status;
	}





}
