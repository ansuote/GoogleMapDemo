package com.lkl.ansuote.demo.googlemapdemo.base.mvp;

import android.content.Intent;
import android.os.Bundle;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

/**
 * Created by huangdongqiang on 15/05/2017.
 */
public abstract class BasePresenter<T> {
    protected Reference<T> mViewRef;

    /**
     * 建立联系
     * @param view
     */
    public void attachView(T view) {
        mViewRef = new WeakReference<T>(view);
    }

    /**
     * 让 View 可以设置默认状态
     */
    public abstract void onStart();

    /**
     * 获取View
     * @return
     */
    protected T getView() {
        if (null != mViewRef) {
            return mViewRef.get();
        }
        return null;
    }

    /**
     * 判断是否建立联系
     * @return
     */
    public boolean isViewAttached(){
        return null != mViewRef && null != mViewRef.get();
    }

    /**
     * 销毁关联的view
     */
    public void detachView() {
        if (null != mViewRef) {
            mViewRef.clear();
            mViewRef = null;
        }
    }

}
