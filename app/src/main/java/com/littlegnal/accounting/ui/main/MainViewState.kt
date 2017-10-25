package com.littlegnal.accounting.ui.main

import com.littlegnal.accounting.base.mvi.MviViewState
import com.littlegnal.accounting.ui.main.adapter.MainAccountingDetail
import java.util.*

/**
 * @author littlegnal
 * @date 2017/8/7
 */
data class MainViewState(
    val lastDate: Date? = null, // 最后一条数据的创建时间，用于查询下一页数据
    val accountingDetailList: List<MainAccountingDetail> = listOf(), // 列表展示
    val error: String? = null, // 错误信息
    val isLoading: Boolean = false, // 是否正在loading
    val isNoData: Boolean = false, // 是否数据库中没有数据
    val isNoMoreData: Boolean = false // 是否还可以加载更多
) : MviViewState

