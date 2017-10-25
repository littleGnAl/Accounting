package com.littlegnal.accounting.base.mvi

import com.hannesdorfmann.mosby3.mvp.MvpView

/**
 * @author littlegnal
 * @date 2017/8/7
 */
interface MviView<in VS : MviViewState>: MvpView {
  fun render(viewState: VS)
}