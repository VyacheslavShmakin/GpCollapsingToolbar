# GooglePlay-CollapsingToolbar
Extended CollapsingToolbar that implemented scrolling behaviour like in Google Play app

Download
--------

Gradle:

```groovy
compile 'com.github.VyacheslavShmakin.gp-collapsing-toolbar:1.0.0'
```

Maven:

```xml
<dependency>
    <groupId>com.github.VyacheslavShmakin</groupId>
    <artifactId>gp-collapsing-toolbar</artifactId>
    <version>1.0.0</version>
    <type>aar</type>
</dependency>
```


Usage
-----
#### In Code
If you wanna enable/disable or check like Google Play app styled behaviour programmatically you should call these methods
``` java
YourGpCollapsingToolbar.setGooglePlayBehaviour(true);
YourGpCollapsingToolbar.isGooglePlayBehaviour();
```

-----
#### In xml

You should use the same parameters that defined in support.design library for CollapsingToolbar with "gp_" prefix
```xml
<ru.shmakinv.android.material.widget.GpCollapsingToolbar
            android:id="@+id/toolbar_layout"
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            android:fitsSystemWindows="true"
            app:gp_collapsedTitleTextAppearance="@style/Toolbar.TitleText"
            app:gp_marketStyledBehaviour="true"
            app:gp_contentScrim="@color/color_primary"
            app:layout_scrollFlags="scroll|enterAlways|enterAlwaysCollapsed"
            app:gp_statusBarScrim="@color/color_primary_dark"
            app:toolbarId="@id/toolbar">
            ...
</ru.shmakinv.android.material.widget.GpCollapsingToolbar>
```