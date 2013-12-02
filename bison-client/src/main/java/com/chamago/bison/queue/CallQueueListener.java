package com.chamago.bison.queue;

/**
 * 
 * @author Gavin.peng
 * 
 * 2013-10-15 下午12:20:13
 × bison
 */
public abstract interface CallQueueListener<E>
{
  public abstract void processQueueElement(E paramE, int paramInt);
}