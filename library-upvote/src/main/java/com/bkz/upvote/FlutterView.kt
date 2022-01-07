package com.bkz.upvote

import android.animation.*
import android.content.Context
import android.graphics.Color
import android.graphics.PointF
import android.util.AttributeSet
import android.view.Gravity
import android.view.animation.*
import android.widget.FrameLayout
import android.widget.ImageView
import java.util.*

class FlutterView @JvmOverloads constructor(
    context: Context,
    attr: AttributeSet? = null,
    defAttr: Int = 0,
) : FrameLayout(context, attr, defAttr) {
    internal var pool: Child? = null
    internal var poolSize = 0
    private val images = mutableListOf<Int>()
    private val interpolates = mutableListOf<Interpolator>()
    private var size = 0
    private var mHeight = 0
    private var mWidth = 0
    private var params: LayoutParams? = null
    private var random = Random()

    init {
        interpolates.add(LinearInterpolator())
        interpolates.add(AccelerateDecelerateInterpolator())
        interpolates.add(AccelerateInterpolator())
        interpolates.add(DecelerateInterpolator())
        setBackgroundColor(Color.TRANSPARENT)
    }

    fun init(size: Int, vararg res: Int) {
        this.size = size
        images.addAll(res.toList())
        params = LayoutParams(size, size)
        params?.gravity = Gravity.CENTER or Gravity.BOTTOM
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        mWidth = measuredWidth
        mHeight = measuredHeight
    }

    fun emit() {
        obtainChild(context).apply {
            target.setImageResource(images[random.nextInt(images.size)])
            target.layoutParams = params
            addView(target)
            target.clearAnimation()
            if (set == null) {
                set = initAnimatorSet(this)
            }
            set?.start()
        }
    }

    private fun initAnimatorSet(child: Child): AnimatorSet {
        val alphaAni = ObjectAnimator.ofFloat(child.target, "alpha", 0.3f, 1f)
        val scaleX = ObjectAnimator.ofFloat(child.target, "scaleX", 0.2f, 1f)
        val scaleY = ObjectAnimator.ofFloat(child.target, "scaleY", 0.2f, 1f)
        val allAnimatorSet = AnimatorSet()
        val createAnimatorSet = AnimatorSet()
        createAnimatorSet.playTogether(alphaAni, scaleX, scaleY)
        createAnimatorSet.duration = 500
        allAnimatorSet.playSequentially(createAnimatorSet, getBezierValueAnimator(child))
        return allAnimatorSet
    }

    private fun getBezierValueAnimator(child: Child): ValueAnimator {
        val x = mWidth / 2f - size / 2f
        val y = mHeight - size.toFloat()
        child.target.x = x
        child.target.y = y
        child.target.alpha = 1f
        val p0 = PointF(x, y)
        val p1 =
            PointF(random.nextInt(mWidth - size).toFloat(), random.nextInt(mHeight / 2).toFloat())
        val p2 = PointF(random.nextInt(mWidth - size).toFloat(),
            random.nextInt(mHeight / 2) + mHeight / 2f)
        val p3 = PointF(random.nextInt(mWidth - size).toFloat(), 0f)
        val evaluator = BezierEvaluator(p1, p2)
        val animator = ValueAnimator.ofObject(evaluator, p0, p3)
        animator.addUpdateListener {
            val point = it.animatedValue as PointF
            child.target.x = point.x
            child.target.y = point.y
            child.target.alpha = (1 - it.animatedFraction)
        }
        animator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animator: Animator) {}
            override fun onAnimationEnd(animator: Animator) {
                removeView(child.target)
                recycle(child)
            }

            override fun onAnimationCancel(animator: Animator) {}
            override fun onAnimationStart(animator: Animator) {}
        })
        animator.setTarget(child.target)
        animator.interpolator = interpolates[random.nextInt(4)]
        animator.duration = 2000
        return animator
    }
}


class Child(
    val target: ImageView,
    var set: AnimatorSet? = null,
    var next: Child? = null,
)

fun FlutterView.obtainChild(context: Context): Child {
    if (pool != null) {
        val f: Child = pool!!
        pool = f.next
        f.next = null
        poolSize--
        return f
    }
    return Child(ImageView(context), null, null)
}

fun FlutterView.recycle(it: Child) {
    if (poolSize < 20) {
        it.next = pool
        pool = it
        poolSize++
    }
}

class BezierEvaluator(private val p1: PointF, private val p2: PointF) : TypeEvaluator<PointF> {
    override fun evaluate(t: Float, p0: PointF, p3: PointF): PointF {
        val point = PointF()
        point.x = (p0.x * (1 - t) * (1 - t) * (1 - t)
                + 3 * p1.x * t * (1 - t) * (1 - t)
                + 3 * p2.x * t * t * (1 - t)
                + p3.x * t * t * t)
        point.y =
            p0.y * (1 - t) * (1 - t) * (1 - t) + 3 * p1.y * t * (1 - t) * (1 - t) + 3 * p2.y * t * t * (1 - t) + p3.y * t * t * t
        return point
    }
}