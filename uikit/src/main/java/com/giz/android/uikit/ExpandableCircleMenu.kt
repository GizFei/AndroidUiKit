/**
 * @author GizFei
 */
package com.giz.android.uikit

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.graphics.Outline
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.*
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import com.giz.android.toolkit.dp2px
import com.giz.android.toolkit.dp2pxSize
import kotlinx.android.synthetic.main.ecm_main_button.view.*
import kotlinx.android.synthetic.main.ecm_menu_button.view.*
import kotlin.math.max
import kotlin.math.min


/**
 * ExpandableCircleMenu：可伸缩圆形按钮菜单，最多支持5个菜单项
 *
 * 可伸缩圆形按钮菜单，ecm_前缀
 *
 * 1、相关布局文件：
 *      - ecm_menu_button.xml：圆形菜单按钮
 *      - ecm_main_button.xml：开关按钮
 *
 * 2、相关资源文件：
 *      - ecm_ripple_effect：菜单按钮背景（圆形 + 涟漪）
 *      - ecm_default_menu_icon：默认菜单按钮
 *      - ecm_default_main_close_icon：默认开关按钮关闭动画图标
 *      - ecm_default_main_open_icon： 默认开关按钮打开动画图标
 *
 * 3、自定义属性：
 * <declare-styleable name="ExpandableCircleMenu">
 *     <!-- 根布局背景颜色，默认#FFF -->
 *     <attr name="ecm_bgColor" format="color" />
 *     <!-- 菜单伸缩时间，默认300 -->
 *     <attr name="ecm_menuToggleTime" format="integer" />
 *     <!--  开关按钮图标 -->
 *     <attr name="ecm_switchIcon" format="reference" />
 *     <!--  单位长度（即根布局高度，决定整个ECM的大小），最大值60dp，最小值32dp -->
 *     <attr name="ecm_unitLength" format="dimension" />
 *     <!--  阴影高度，默认8dp -->
 *     <attr name="ecm_elevation" format="dimension" />
 * </declare-styleable>
 *
 * 4、使用示例：
 * <com.giz.android.uikit.ExpandableCircleMenu
 *      android:layout_width="wrap_content"         // 定值无效
 *      android:layout_height="wrap_content"        // 定值无效
 *      app:ecm_bgColor="#FFF"
 *      app:ecm_menuToggleTime="300"
 *      app:ecm_switchIcon="@drawable/ic_drive_eta_white"
 *      app:ecm_unitLength="60dp">
 *
 *      <ImageView
 *          android:id="@+id/ecm_item1"             // 用于区别菜单项
 *          android:layout_width="wrap_content"     // 无效，由unitLength计算得出
 *          android:layout_height="wrap_content"    // 无效，由unitLength计算得出
 *          android:src="@drawable/ic_edit_white"   // 菜单项图标
 *          android:backgroundTint="#888"           // 菜单项背景色，默认#888
 *      />
 *
 *      <ImageView
 *          android:id="@+id/ecm_item2"
 *          android:layout_width="wrap_content"
 *          android:layout_height="wrap_content"
 *          android:src="@drawable/ic_drive_eta_white"
 *          android:backgroundTint="#888"
 *      />
 * </com.giz.android.uikit.ExpandableCircleMenu>
 */
class ExpandableCircleMenu(context: Context, attrSet: AttributeSet?, defStyleAttrs: Int)
    : LinearLayout(context, attrSet, defStyleAttrs) {

    private val TAG = "ExpandableCircleMenu"
    private val mContext: Context = context

    /**
     * 菜单是否为展开状态
     */
    var isExpanded = false
    /**
     * 菜单是否可见
     */
    var isVisible = true
    // 过程变量
    private var mIsFirstExpand = true
    private var mIsFirstToggle = false
    private var mOriginWidth = 0
    private var mFinalWidth = 0
    private var mTmpAnimWidth = 0
    private var mMenuBtnAnimDuration = 200
    // 自定义属性变量值
    /**
     * 菜单展开与折叠的动画时间
     */
    var mMenuAnimDuration: Int = 300
    private var mBgColor: Int = Color.WHITE
    private var mSwitchIcon: Drawable? = null
    private var mUnitLength: Int = dp2pxSize(context, 60f)
    private var mElevation: Float = dp2px(context, 8f)

    private val mIconImgView: ArrayList<ImageView> = arrayListOf()
    private lateinit var mMainBtn: View
    /**
     * 菜单项点击事件监听器
     */
    var mMenuItemClickListener: OnMenuItemClickListener? = null

    constructor(context: Context) : this(context, null, 0)
    constructor(context: Context, attrSet: AttributeSet?) : this(context, attrSet, 0)
    // 初始化自定义属性
    init {
        val typedArray = context.theme.obtainStyledAttributes(attrSet, R.styleable.ExpandableCircleMenu, defStyleAttrs, 0)

        mBgColor = typedArray.getColor(R.styleable.ExpandableCircleMenu_ecm_bgColor, mBgColor)
        mMenuAnimDuration = typedArray.getInt(R.styleable.ExpandableCircleMenu_ecm_menuToggleTime, mMenuAnimDuration)
        mSwitchIcon = typedArray.getDrawable(R.styleable.ExpandableCircleMenu_ecm_switchIcon)
        val maxUnitLength = dp2pxSize(context, 60f) //最大单位长
        val minUnitLength = dp2pxSize(context, 32f) // 最小单位长
        mUnitLength = max(min(typedArray.getDimensionPixelSize(R.styleable.ExpandableCircleMenu_ecm_unitLength, mUnitLength), maxUnitLength), minUnitLength)
        mElevation = typedArray.getDimension(R.styleable.ExpandableCircleMenu_ecm_elevation, mElevation)

        typedArray.recycle()
    }
    init {
        // 初始化根布局样式
        orientation = HORIZONTAL
        elevation = mElevation
        setBackgroundColor(mBgColor)
        gravity = Gravity.CENTER_VERTICAL
        clipToOutline = true
        outlineProvider = object : ViewOutlineProvider(){
            override fun getOutline(view: View, outline: Outline) {
                outline.setRoundRect(0, 0, view.width, view.height, dp2px(context, mUnitLength / 2.0f))
                outline.alpha = 0.5f
            }
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        val count: Int = min(childCount, 5)
        for(i in 0 until count){
            mIconImgView.add(getChildAt(i) as ImageView)
        }
        removeAllViewsInLayout()
        // 添加开关按钮
        val tmpRootLayout = FrameLayout(mContext)
        mMainBtn = LayoutInflater.from(mContext).inflate(R.layout.ecm_main_button, tmpRootLayout, false)
        mMainBtn.setBackgroundColor(mBgColor)
        val iconView: ImageView = mMainBtn.ecm_main_button_icon
        if(mSwitchIcon != null){
            iconView.setImageDrawable(mSwitchIcon)
        }
        // 设置宽高
        mMainBtn.layoutParams.width  = mUnitLength
        mMainBtn.layoutParams.height = mUnitLength
        iconView.layoutParams.width  = (mUnitLength * 0.6f).toInt()
        iconView.layoutParams.height = (mUnitLength * 0.6f).toInt()
        mMainBtn.ecm_main_button_divider.layoutParams.height = mUnitLength / 2
        mMainBtn.setOnClickListener {
            if(mIsFirstExpand){
                mIsFirstExpand = false
                initMenuLayout()
            }else{
                toggleExpandable()
            }
        }
        addView(mMainBtn)
        // 使具体的宽高数值无效
        layoutParams.width  = LayoutParams.WRAP_CONTENT
        layoutParams.height = LayoutParams.WRAP_CONTENT
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        // 布局
        super.onLayout(changed, l, t, r, b)
        // 保证开关按钮位置不动
        mMainBtn.layout(measuredWidth - measuredHeight, 0, measuredWidth, measuredHeight)
        if(mIsFirstToggle){
            mIsFirstToggle = false
            toggleExpandable()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // 测量
        measureChildren(widthMeasureSpec, heightMeasureSpec)
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        if (mOriginWidth == 0) {
            mOriginWidth = measuredWidth
        }
        if (!mIsFirstExpand && mFinalWidth == 0) {
            mFinalWidth = measuredWidth
            setMeasuredDimension(mOriginWidth, measuredHeight)
            mIsFirstToggle = true
        }
    }

    /* ===================== */
    /* 折叠与展开菜单         */
    /* ===================== */
    private fun toggleExpandable(){
        when(isExpanded) {
            true  -> collapseMenu()
            false -> expandMenu()
        }
    }
    private fun collapseMenu(){
        isExpanded = false
        mMainBtn.ecm_main_button_divider.animate().alpha(0f).setDuration(mMenuAnimDuration.toLong()).start()
        if(mSwitchIcon != null){
            mMainBtn.ecm_main_button_icon.animate().setDuration(360).rotation(0f)
        }else{
            val avd = AnimatedVectorDrawableCompat.create(mContext, R.drawable.ecm_default_main_close_icon)
            mMainBtn.ecm_main_button_icon.setImageDrawable(avd)
            avd?.start()
        }
        val animator = ValueAnimator.ofInt(min(mTmpAnimWidth, mFinalWidth), mOriginWidth)
        animator.duration = mMenuAnimDuration.toLong()
        animator.addUpdateListener {
            mTmpAnimWidth = it.animatedValue as Int
            layoutParams.width = mTmpAnimWidth
            requestLayout()
        }
        animator.start()
    }
    private fun expandMenu(){
        isExpanded = true
        mMainBtn.ecm_main_button_divider.animate().alpha(1f).setDuration(mMenuAnimDuration.toLong()).start()
        if(mSwitchIcon != null){
            mMainBtn.ecm_main_button_icon.animate().setDuration(360).rotation(-90f)
        }else{
            val avd = AnimatedVectorDrawableCompat.create(mContext, R.drawable.ecm_default_main_open_icon)
            mMainBtn.ecm_main_button_icon.setImageDrawable(avd)
            avd?.start()
        }
        val animator = ValueAnimator.ofInt(max(mTmpAnimWidth, mOriginWidth), mFinalWidth)
        animator.duration = mMenuAnimDuration.toLong()
        animator.addUpdateListener {
            mTmpAnimWidth = it.animatedValue as Int
            layoutParams.width = mTmpAnimWidth
            requestLayout()
        }
        animator.start()
    }

    /* ===================== */
    /* 初始化菜单项           */
    /* ===================== */
    private fun initMenuLayout(){
        // 动态添加View
        val tmpRootLayout = FrameLayout(mContext)
        mIconImgView.forEachIndexed { index, imageView ->
            val menuBtn = LayoutInflater.from(mContext).inflate(R.layout.ecm_menu_button, tmpRootLayout, false)
            val iconView = menuBtn.ecm_menu_button_icon as ImageView
            val bgView = menuBtn.ecm_menu_button_bg
            iconView.id = imageView.id
            if(imageView.drawable != null){
                iconView.setImageDrawable(imageView.drawable)
            }
            if(imageView.backgroundTintList != null){
                bgView.backgroundTintList = imageView.backgroundTintList
            }
            // 改变宽高
            menuBtn.layoutParams.width  = mUnitLength
            menuBtn.layoutParams.height = mUnitLength
            bgView.layoutParams.width  = (mUnitLength * 2 / 3f).toInt()
            bgView.layoutParams.height = (mUnitLength * 2 / 3f).toInt()
            iconView.layoutParams.width  = (mUnitLength * 0.4f).toInt()
            iconView.layoutParams.height = (mUnitLength * 0.4f).toInt()

            addView(menuBtn, index)
        }
        setupMenuBtnListeners()
        requestLayout()
    }
    private fun setupMenuBtnListeners(){
        val minBtnScale = 0.8f
        for(i in 0 until childCount-1){
            getChildAt(i).setOnClickListener {
                collapseMenu()
                mMenuItemClickListener?.onClick(it, (it as FrameLayout).getChildAt(1).id)
            }
            getChildAt(i).setOnTouchListener{ v, event ->
                v.animate().duration = mMenuBtnAnimDuration.toLong()
                when(event.action){
                    MotionEvent.ACTION_DOWN -> v.animate().scaleX(minBtnScale).scaleY(minBtnScale).setListener(null).start()
                    MotionEvent.ACTION_UP -> {
                        if(v.scaleX != minBtnScale){
                            v.animate().setListener(object : AnimatorListenerAdapter(){
                                override fun onAnimationEnd(animation: Animator?) {
                                    v.animate().scaleX(1.0f).scaleY(1.0f).setListener(object : AnimatorListenerAdapter(){
                                        override fun onAnimationEnd(animation: Animator?) {
                                            v.animate().setListener(null)
                                        }
                                    }).start()
                                }
                            })
                        }else{
                            v.animate().scaleX(1.0f).scaleY(1.0f).setListener(object : AnimatorListenerAdapter(){
                                override fun onAnimationEnd(animation: Animator?) {
                                    v.animate().setListener(null)
                                }
                            }).start()
                        }
                    }
                    MotionEvent.ACTION_MOVE -> {
                        if(event.x.toInt() !in 0..v.width || event.y.toInt() !in 0..v.height){
                            // 滑出控件
                            v.animate().scaleX(1.0f).scaleY(1.0f).setListener(null).start()
                        }
                    }
                }
                false
            }
        }
    }

    /* ===================== */
    /* 显示与隐藏菜单         */
    /* ===================== */
    /**
     * 交替隐藏或显示菜单
     */
    fun toggleVisible() {
        when(isVisible){
            true  -> hide()
            false -> show()
        }
    }
    private fun hide() {
        // 隐藏菜单
        isVisible = false
        this.animate().alpha(0f).setDuration(mMenuAnimDuration.toLong())
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    visibility = View.GONE
                    animate().setListener(null)
                }
            }).start()
    }
    private fun show() {
        // 显示菜单
        isVisible = true
        this.animate().alpha(1f).setDuration(mMenuAnimDuration.toLong())
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator?) {
                    visibility = VISIBLE
                }
                override fun onAnimationEnd(animation: Animator?) {
                    animate().setListener(null)
                }
            }).start()
    }

    /**
     * 菜单项按钮点击事件监听器
     */
    interface OnMenuItemClickListener {
        /**
         * 点击回调函数
         * @param v 视图
         * @param menuId 在<ExpandableCircleMenu>下添加的子ImageView的Id，如果没有设置，
         *               则为com.giz.androiduikit.R.id.ecm_menu_button_icon = -1000141
         */
        fun onClick(v: View, menuId: Int)
    }
}