package com.littlegnal.accounting.base.mvibase

/**
 * 异步操作阶段状态：Loading, Content, Error
 * @author littlegnal
 * @date 2017/12/14
 */
enum class LceStatus {
  /**
   * 异步操作成功
   */
  SUCCESS,
  /**
   * 异步操作失败
   */
  FAILURE,
  /**
   * 异步操作正在进行
   */
  IN_FLIGHT
}