# 蝌蚪听故事

## 喜马拉雅问题

1. 大小喜马的含义？

## 悬浮窗问题

WindowManager添加的视图会创建一个新的window，该window下的触摸事件不能传递到Activity的Window，
因此在悬浮窗收起时，悬浮窗的占位部分Activity无法收到触摸事件。

修复方案：将悬浮视图直接添加到activity的视图层级下