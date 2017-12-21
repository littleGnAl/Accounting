package com.yunmai.scale.coach.common.extensions

import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

/**
 * @author littlegnal
 * @date 2017/12/14
 */

/**
 * 可以使用`+=`来添加[Disposable]
 */
operator fun CompositeDisposable.plusAssign(disposable: Disposable) {
  this.add(disposable)
}
