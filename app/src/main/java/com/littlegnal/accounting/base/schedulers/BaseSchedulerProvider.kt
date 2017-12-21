package com.littlegnal.accounting.base.schedulers

import io.reactivex.Scheduler

/**
 * [Scheduler]提供类，方便统一管理
 * @author littlegnal
 * @date 2017/12/11
 */
interface BaseSchedulerProvider {

  fun computation(): Scheduler

  fun io(): Scheduler

  fun ui(): Scheduler
}