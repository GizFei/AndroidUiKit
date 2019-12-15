# Androit Ui Kit

Custom views created by Giz.

#### 使用方法

在Module下的`build.gradle`文件添加：

```xml
dependencies {
	...
	implementation 'com.giz.android:android-ui-kit:1.0.0'
}
```

## ExpandableCircleMenu

ExpandableCircleMenu：可伸缩圆形按钮菜单，最多支持5个菜单项

#### 效果图

<html>

<img src="assets/1576407512.gif" max-width="50%" width="50%" repeat="true"/>

</html>

#### 自定义样式属性

```xml
<declare-styleable name="ExpandableCircleMenu">
	<!-- 根布局背景颜色，默认#FFF -->
	<attr name="ecm_bgColor" format="color" />
	<!-- 菜单伸缩时间，默认300 -->
	<attr name="ecm_menuToggleTime" format="integer" />
	<!--  开关按钮图标 -->
    <attr name="ecm_switchIcon" format="reference" />
    <!--  单位长度（即根布局高度，决定整个ECM的大小），最大值60dp，最小值32dp -->
    <attr name="ecm_unitLength" format="dimension" />
    <!--  阴影高度，默认8dp -->
    <attr name="ecm_elevation" format="dimension" />
</declare-styleable>
```

#### 使用示例

```xml
<com.giz.android.uikit.ExpandableCircleMenu
    android:layout_width="wrap_content"         // 定值无效
    android:layout_height="wrap_content"        // 定值无效
    app:ecm_bgColor="#FFF"
    app:ecm_menuToggleTime="300"
    app:ecm_switchIcon="@drawable/ic_drive_eta_white"
    app:ecm_unitLength="60dp">
    
    <ImageView
        android:id="@+id/ecm_item1"             // 用于区别菜单项
        android:layout_width="wrap_content"     // 无效，由unitLength计算得出
        android:layout_height="wrap_content"    // 无效，由unitLength计算得出
        android:src="@drawable/ic_edit_white"   // 菜单项图标
        android:backgroundTint="#888"           // 菜单项背景色，默认#888
    />

    <ImageView
        android:id="@+id/ecm_item2"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:src="@drawable/ic_drive_eta_white"
        android:backgroundTint="#888"
    />
</com.giz.android.uikit.ExpandableCircleMenu>
```