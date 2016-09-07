package com.fort.xrock.listener;

/**
 * Created by Mac on 16/9/6.
 * 根据Adapter的绑定index，判断是否到达列表底部
 */
public abstract class AdapterScrollListener {

    int diffNum = 1;
    boolean loading = false;

    /**
     * 设置距离底部的间隔数
     *
     * @param num
     */
    public void setDiffNum(int num) {
        diffNum = num;
    }

    /**
     * 在adapter的绑定数据时调用此方法
     *
     * @param count
     * @param bindIndex
     */
    public void onBind(int count, int bindIndex) {
        if (!loading && count > 0 && bindIndex >= count - diffNum) {
            loading = true;
            onBind(count, bindIndex);
        }
    }

    /**
     * 设置可用状态
     *
     * @param loading
     */
    public void setStatus(boolean loading) {
        this.loading = loading;
    }

    /**
     * 滑到adapter底部
     *
     * @param count
     * @param index
     */
    public abstract void onBottom(int count, int index);
}