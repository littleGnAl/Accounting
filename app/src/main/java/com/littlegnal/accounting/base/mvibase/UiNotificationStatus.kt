package com.littlegnal.accounting.base.mvibase

/**
 * 像弹窗，toast或者动画等，很多时候我们只需要显示一次，所以通过该状态来控制
 * * [SHOW] : 显示
 * * [NONE] : 不显示
 * @author littlegnal
 * @date 2017/12/14
 */
enum class UiNotificationStatus {
  SHOW, NONE
}