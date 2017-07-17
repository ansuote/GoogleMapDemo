package com.lkl.ansuote.demo.googlemapdemo.base.mvp;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

/**
 * MVP 架构框架类
 * Created by huangdongqiang on 15/05/2017.
 */
public abstract class BaseMVPActivity<V, T extends BasePresenter<V>> extends AppCompatActivity {
    protected T mPresenter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initView();
        mPresenter = createPresenter();
        initVariables(savedInstanceState);

        if (null != mPresenter) {
            mPresenter.attachView((V)this);
            mPresenter.onStart();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != mPresenter) {
            mPresenter.detachView();
        }
    }

    /**
     * 创建 Presenter
     * @return
     */
    protected abstract T createPresenter();

    /**
     * 初始化界面
     */
    protected abstract void initView();

    /**
     * 初始化变量
     */
    protected abstract void initVariables(Bundle savedInstanceState);
}
