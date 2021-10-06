# HexoToTypecho
hexo文章markdown文件转至typecho
# 功能
只简单迁移文章的`分类`,`标签`,`内容`,`发布时间`,`修改图片链接`
分类和标签必须提前写入数据库中,不能自动增加
# bug
暂时图片链接如果有中文会无效
...
# 使用
需要支持maven,mysql数据库
1.修改c3p0-config.xml文件相关的用户名,密码等
2.StartBoot.java的mian方法中修改hexo的_post路径
