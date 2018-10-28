/*
 * Copyright (C) 2017 littlegnal
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.littlegnal.accounting.ui.summary

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import com.littlegnal.accounting.R
import com.littlegnal.accounting.base.util.colorRes
import com.littlegnal.accounting.base.util.dip
import com.littlegnal.accounting.base.util.sp
import io.reactivex.subjects.PublishSubject
import java.util.Date

/**
 * 汇总曲线控件
 */
class SummaryChart : View {

  var months: List<Pair<String, Date>>? = null

  private var max: Float = -1.0f

  private var min: Float = -1.0f

  var points: List<Pair<Int, Float>>? = null
    set(value) {
      field = value
      max = value?.maxBy { it.second }?.second ?: -1.0f
      min = value?.minBy { it.second }?.second ?: -1.0f
    }

  var selectedIndex: Int = -1

  var values: List<String>? = null

  private val DOT_RADIUS = dip(3)
  private val SELECTED_DOT_RADIUS = dip(6)

  private val textBounds: Rect by lazy { Rect() }

  private val linePaint: Paint by lazy {
    Paint(Paint.ANTI_ALIAS_FLAG)
        .apply {
          color = context.colorRes(R.color.colorAccent)
          strokeWidth = dip(2).toFloat()
          style = Paint.Style.STROKE
        }
  }

  private val monthsPaint: Paint by lazy {
    Paint(Paint.ANTI_ALIAS_FLAG)
        .apply {
          textSize = sp(14).toFloat()
          color = context.colorRes(R.color.defaultTextColor)
        }
  }

  private val valueTipsTextPaint by lazy {
    Paint(monthsPaint).apply {
      color = context.colorRes(R.color.colorAccent)
    }
  }

  private val valueTipsPaint by lazy {
    Paint(Paint.ANTI_ALIAS_FLAG)
        .apply {
          color = 0xfff3f3f3.toInt()
          style = Paint.Style.FILL
        }
  }

  private val dotPaint: Paint by lazy {
    Paint(Paint.ANTI_ALIAS_FLAG)
        .apply {
          style = Paint.Style.FILL
          color = context.colorRes(R.color.colorAccent)
        }
  }

  private var activePointerId: Int = -1
  private var lastDownX: Float = -1.0f
  private var lastDownY: Float = -1.0f
  private var touchSlop: Int = -1
  private var isMoved: Boolean = false

  private val TOUCH_RADIUS = dip(20)

  private val curvePath: Path by lazy { Path() }

  private val trianglePath: Path by lazy { Path() }

  private val valueTipsRectF: RectF by lazy { RectF() }

  private val monthClickedPublisher: PublishSubject<Date> = PublishSubject.create()

  constructor(context: Context?) : this(context, null)

  constructor(
    context: Context?,
    attrs: AttributeSet?
  ) : this(context, attrs, 0)

  constructor(
    context: Context?,
    attrs: AttributeSet?,
    defStyleAttr: Int
  ) :
      super(context, attrs, defStyleAttr) {
    touchSlop = ViewConfiguration.get(context)
        .scaledTouchSlop
  }

  override fun onDraw(canvas: Canvas?) {
    drawCurveLine(canvas)
    drawDotsAndTips(canvas)
    drawMonths(canvas)
  }

  /**
   * 只用 *40%* 的高度画曲线, 上下留出 *30%* 的间隔
   * @see getYByValue
   */
  private fun drawCurveLine(canvas: Canvas?) {
    points?.let {
      if (it.isEmpty()) return
      var preX: Float
      var preY: Float
      var curX: Float
      var curY: Float
      val firstPoint = it[0]
      curX = getXByIndex(firstPoint.first)
      curY = getYByValue(firstPoint.second)
      curvePath.moveTo(curX, curY)
      for (pair in it) {
        preX = curX
        preY = curY
        curX = getXByIndex(pair.first)
        curY = getYByValue(pair.second)
        val cpx: Float = preX + (curX - preX) / 2.0f
        curvePath.cubicTo(cpx, preY, cpx, curY, curX, curY)
      }

      canvas?.drawPath(curvePath, linePaint)
    }
  }

  private fun drawDotsAndTips(canvas: Canvas?) {
    points?.apply {
      val triangleHeight = dip(5)
      val triangleWidth = dip(10)
      val triangleDotMargin = dip(6)
      val rectRadius = dip(5).toFloat()

      for ((index, pair) in this.withIndex()) {
        val valueIndex = pair.first
        val x = getXByIndex(valueIndex)
        val y = getYByValue(pair.second)
        if (valueIndex == selectedIndex) {
          canvas?.drawCircle(x, y, SELECTED_DOT_RADIUS.toFloat(), dotPaint)
        } else {
          canvas?.drawCircle(x, y, DOT_RADIUS.toFloat(), dotPaint)
        }

        values?.get(index)
            ?.apply {
              val valueWidth = getTextWith(this, valueTipsTextPaint)
              val valueHeight = getTextHeight(this, valueTipsTextPaint)

              if (valueIndex == selectedIndex) {
                trianglePath.reset()
                trianglePath.moveTo(x, y - triangleDotMargin - SELECTED_DOT_RADIUS / 2.0f)
                trianglePath.lineTo(
                    x - triangleWidth / 2.0f,
                    y - triangleDotMargin - triangleHeight - SELECTED_DOT_RADIUS / 2.0f
                )
                trianglePath.lineTo(
                    x + triangleWidth / 2.0f,
                    y - triangleDotMargin - triangleHeight - SELECTED_DOT_RADIUS / 2.0f
                )

                val rectWidth = valueWidth + dip(6) * 2.0f
                val rectHeight = valueHeight + dip(6) * 2.0f

                valueTipsRectF.setEmpty()
                valueTipsRectF.left = x - rectWidth / 2.0f
                valueTipsRectF.right = valueTipsRectF.left + rectWidth
                valueTipsRectF.bottom = y - triangleDotMargin - triangleHeight -
                    SELECTED_DOT_RADIUS / 2.0f
                valueTipsRectF.top = valueTipsRectF.bottom - rectHeight
                trianglePath.addRoundRect(valueTipsRectF, rectRadius, rectRadius, Path.Direction.CW)
                canvas?.drawPath(trianglePath, valueTipsPaint)

                valueTipsTextPaint.color = context.colorRes(R.color.colorAccent)
                canvas?.drawText(
                    this,
                    valueTipsRectF.centerX() - valueWidth / 2.0f,
                    valueTipsRectF.centerY() + valueHeight / 2.0f,
                    valueTipsTextPaint
                )
              } else {
                valueTipsTextPaint.color = context.colorRes(R.color.defaultTextColor)
                canvas?.drawText(
                    this,
                    x - valueWidth / 2.0f,
                    y - triangleDotMargin - DOT_RADIUS / 2.0f,
                    valueTipsTextPaint
                )
              }
            }
      }
    }
  }

  /**
   * 底部留*40dp*画日期
   */
  private fun drawMonths(canvas: Canvas?) {
    months?.apply {
      for ((index, value) in this.withIndex()) {
        val month: String = value.first
        val x = getXByIndex(index) - getTextWith(month, monthsPaint) / 2.0f
        val y = height - dip(40) / 2.0f + getTextHeight(month, monthsPaint) / 2.0f
        canvas?.drawText(month, x, y, monthsPaint)
      }
    }
  }

  private fun getXByIndex(index: Int): Float {
    val itemSpacing = getItemSpacing()
    return itemSpacing * index + itemSpacing / 2.0f
  }

  private fun getItemSpacing() = (width / months?.size!!).toFloat()

  private fun getYByValue(value: Float): Float {
    return if (max == -1.0f || min == -1.0f) {
      0.0f
    } else if (max == min) {
      (height - dip(40)) / 2.0f
    } else {
      val drawingHeight = height - dip(40)
      val availableDrawingHeight = drawingHeight * 0.4f
      drawingHeight - (drawingHeight * 0.3f) -
          (availableDrawingHeight / (max - min)) * (value - min)
    }
  }

  @SuppressLint("ClickableViewAccessibility")
  override fun onTouchEvent(event: MotionEvent?): Boolean {
    when (event?.actionMasked) {
      MotionEvent.ACTION_DOWN -> {
        activePointerId = event.getPointerId(0)
        lastDownX = event.x
        lastDownY = event.y
      }
      MotionEvent.ACTION_MOVE -> {
        val deltaX = lastDownX - event.getX(activePointerId)
        val deltaY = lastDownY - event.getY(activePointerId)
        if (Math.abs(deltaX) > touchSlop || Math.abs(deltaY) > touchSlop) {
          isMoved = true
        }
      }
      MotionEvent.ACTION_UP -> {
        if (!isMoved) {
          val itemSpacing = getItemSpacing()
          val x = event.x
          val y = event.y
          val index: Int = (x / getItemSpacing()).toInt()

          points?.filter { it.first == index }
              ?.take(1)
              ?.forEach {
                val valueX = index * itemSpacing + itemSpacing / 2.0f

                if (Math.abs(x - valueX) <= TOUCH_RADIUS / 2.0f &&
                    Math.abs(y - getYByValue(it.second)) <= TOUCH_RADIUS / 2.0f
                ) {
                  months?.get(index)
                      ?.apply {
                        selectedIndex = index
                        postInvalidate()
                        monthClickedPublisher.onNext(this.second)
                      }
                }
              }
        }

        isMoved = false
      }
    }

    return true
  }

  private fun getTextWith(
    text: String,
    paint: Paint
  ): Float =
    paint.measureText(text, 0, text.length)

  private fun getTextHeight(
    text: String,
    paint: Paint
  ): Int {
    textBounds.setEmpty()
    paint.getTextBounds(text, 0, text.length, textBounds)

    return textBounds.height()
  }

  fun getMonthClickedObservable() = monthClickedPublisher
}
