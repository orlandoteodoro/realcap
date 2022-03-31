/*
 * This file is auto-generated.  DO NOT MODIFY.
 */
package br.com.realcapreal.realcapvendas.service;
/**
 * 打印服务执行结果的回调
 */
public interface IPosPrinterCallback extends android.os.IInterface
{
  /** Default implementation for IPosPrinterCallback. */
  public static class Default implements IPosPrinterCallback
  {
    /**
    	* 返回执行结果
    	* @param isSuccess:	  true执行成功，false 执行失败
    	*/
    @Override
    public void onRunResult(boolean isSuccess) throws android.os.RemoteException
    {
    }
    /**
    	* 返回结果(字符串数据)
    	* @param result:	结果，打印机上电以来打印长度(单位mm)
    	*/
    @Override
    public void onReturnString(String result) throws android.os.RemoteException
    {
    }
    @Override
    public android.os.IBinder asBinder() {
      return null;
    }
  }
  /** Local-side IPC implementation stub class. */
  public static abstract class Stub extends android.os.Binder implements IPosPrinterCallback
  {
    private static final String DESCRIPTOR = "com.iposprinter.iposprinterservice.IPosPrinterCallback";
    /** Construct the stub at attach it to the interface. */
    public Stub()
    {
      this.attachInterface(this, DESCRIPTOR);
    }
    /**
     * Cast an IBinder object into an com.iposprinter.iposprinterservice.IPosPrinterCallback interface,
     * generating a proxy if needed.
     */
    public static IPosPrinterCallback asInterface(android.os.IBinder obj)
    {
      if ((obj==null)) {
        return null;
      }
      android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
      if (((iin!=null)&&(iin instanceof IPosPrinterCallback))) {
        return ((IPosPrinterCallback)iin);
      }
      return new Proxy(obj);
    }
    @Override
    public android.os.IBinder asBinder()
    {
      return this;
    }
    @Override
    public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
    {
      String descriptor = DESCRIPTOR;
      switch (code)
      {
        case INTERFACE_TRANSACTION:
        {
          reply.writeString(descriptor);
          return true;
        }
        case TRANSACTION_onRunResult:
        {
          data.enforceInterface(descriptor);
          boolean _arg0;
          _arg0 = (0!=data.readInt());
          this.onRunResult(_arg0);
          return true;
        }
        case TRANSACTION_onReturnString:
        {
          data.enforceInterface(descriptor);
          String _arg0;
          _arg0 = data.readString();
          this.onReturnString(_arg0);
          return true;
        }
        default:
        {
          return super.onTransact(code, data, reply, flags);
        }
      }
    }
    private static class Proxy implements IPosPrinterCallback
    {
      private android.os.IBinder mRemote;
      Proxy(android.os.IBinder remote)
      {
        mRemote = remote;
      }
      @Override
      public android.os.IBinder asBinder()
      {
        return mRemote;
      }
      public String getInterfaceDescriptor()
      {
        return DESCRIPTOR;
      }
      /**
      	* 返回执行结果
      	* @param isSuccess:	  true执行成功，false 执行失败
      	*/
      @Override
      public void onRunResult(boolean isSuccess) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeInt(((isSuccess)?(1):(0)));
          boolean _status = mRemote.transact(Stub.TRANSACTION_onRunResult, _data, null, android.os.IBinder.FLAG_ONEWAY);
          if (!_status && getDefaultImpl() != null) {
            getDefaultImpl().onRunResult(isSuccess);
            return;
          }
        }
        finally {
          _data.recycle();
        }
      }
      /**
      	* 返回结果(字符串数据)
      	* @param result:	结果，打印机上电以来打印长度(单位mm)
      	*/
      @Override
      public void onReturnString(String result) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeString(result);
          boolean _status = mRemote.transact(Stub.TRANSACTION_onReturnString, _data, null, android.os.IBinder.FLAG_ONEWAY);
          if (!_status && getDefaultImpl() != null) {
            getDefaultImpl().onReturnString(result);
            return;
          }
        }
        finally {
          _data.recycle();
        }
      }
      public static IPosPrinterCallback sDefaultImpl;
    }
    static final int TRANSACTION_onRunResult = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
    static final int TRANSACTION_onReturnString = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
    public static boolean setDefaultImpl(IPosPrinterCallback impl) {
      if (Proxy.sDefaultImpl == null && impl != null) {
        Proxy.sDefaultImpl = impl;
        return true;
      }
      return false;
    }
    public static IPosPrinterCallback getDefaultImpl() {
      return Proxy.sDefaultImpl;
    }
  }
  /**
  	* 返回执行结果
  	* @param isSuccess:	  true执行成功，false 执行失败
  	*/
  public void onRunResult(boolean isSuccess) throws android.os.RemoteException;
  /**
  	* 返回结果(字符串数据)
  	* @param result:	结果，打印机上电以来打印长度(单位mm)
  	*/
  public void onReturnString(String result) throws android.os.RemoteException;
}
