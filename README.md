#  Mongodb-WeAdmin

[![QQ群](https://img.shields.io/badge/QQ%E7%BE%A4-924715723-yellowgreen.svg)](https://jq.qq.com/?_wv=1027&k=5PIRvFq)
[![码云](https://img.shields.io/badge/Gitee-%E7%A0%81%E4%BA%91-yellow.svg)](https://gitee.com/qinxuewu)
[![Github](https://img.shields.io/badge/Github-Github-red.svg)](https://github.com/a870439570)


#### 项目介绍
- Mongodb网页管理工具,基于Spring Boot2.0，前端采用layerUI实现。
- 源于线上环境部署mongodb时屏蔽了外网访问mongodb,所以使用不了mongochef这样方便的远程连接工具，便Mongodb提供的java api实现的的网页版管理
- 未设置登录权限相关模块，低耦合性 方便嵌入到现有的项目中


#### 软件架构
1. springBoot2.0相关组件
1. mongodb
1. layerUI

### 功能说明
- Mongodb的多数据库查询
- Mongodb的多数据对应下的表查询
- Mongodb的指定表下的数据列表查询
- Mongodb集合的增删改查
- Mongodb集合的导出表数据为格式化json文件
- MongoDB 条件操作符查询 ` $gt, $lt, $gte,  $lte, $ne, $in, $regex  等等基本条件 `
- MongoDB 聚合 aggregate() 方法

### 启动运行
1. application.properties 配置mongodb服务地址
1. 运行启动Application.java类
1. 访问 http://localhost:8080/login.html 默认账号密码admin,admin（application.properties配置文件可以修改）

### 效果图如下

### 登录
![输入图片说明](https://images.gitee.com/uploads/images/2018/1121/153436_4417ccb3_1478371.png "屏幕截图.png")

### 显示所有的数据源
![输入图片说明](https://images.gitee.com/uploads/images/2018/1122/153203_c35f73df_1478371.png "屏幕截图.png")

### 显示指定数据源下的表
![输入图片说明](https://images.gitee.com/uploads/images/2018/1121/153553_91a7ba66_1478371.png "屏幕截图.png")

### 查询指定表的数据
![输入图片说明](https://images.gitee.com/uploads/images/2018/1122/153243_d170a1ee_1478371.png "屏幕截图.png")


### 条件查询方法(组装成json数据请求后台)
```
查询指定的key: {"openid": "owibYt4P6Yu7gzsKdamO8CtRGxNk"}

使用 (<=) 和 (>=) 查询 {"createtime": {$gte: "2017-01-27 18:24:38", $lte: "2017-05-27 18:24:38"}}

使用  $in 查询 {"id":{$in:"1,2,3,4,5,6"}

使用  $regex 模糊查询  {"city":{"$regex":"广"}

使用 多条件组合查询 {"city":{"$regex":"广"},"province":"广东","subscribe":1,"sourceType":{"$in":["304517"]}}

更多查询条件 参考  http://www.runoob.com/mongodb/mongodb-window-install.html
```
![输入图片说明](https://images.gitee.com/uploads/images/2018/1121/102443_d1388d16_1478371.png "屏幕截图.png")


### 查询条件+aggregate聚合分组
$group 后面是分组的条件和分组后指定显示的字段（mongodb分组查询必须指定显示字段，不然只会显示分组的字段）
```
{
	"date": {
		"$gte": "2017-01-27 18:24:38"        #分组前筛选的条件 比如 大于等于指定时间内的数据
	},
	"$group": {
		"_id": "$stores",                   #分组的字段（默认要加$符号），多个字段分组使用json格式包装起来
		"count": {
			"$sum": 1                   #统计分组字段出现的次数，比如$avg,$max等等aggregate自带的统计符号
		},
		"stores": {
			"$first": "$stores"        #指定分组统计后需要显示的字段，如果不指定只会显示分组的字段合统计的总数
		},
		"phone": {
			"$first": "$phone"       #指定分组统计后需要显示的字段，如果不指定只会显示分组的字段合统计的总数
		}
	}
}
```
![输入图片说明](https://images.gitee.com/uploads/images/2018/1122/125607_6d3cf2c0_1478371.png "屏幕截图.png")



### 添加 删除，修改(添加，修改后点击右上角刷新按钮)
![输入图片说明](https://images.gitee.com/uploads/images/2018/1121/102729_8c3892a1_1478371.png "屏幕截图.png")
![输入图片说明](https://images.gitee.com/uploads/images/2018/1121/102746_e486eb86_1478371.png "屏幕截图.png")


