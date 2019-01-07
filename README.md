# Economy
简单的MC服务器经济插件。

## 特性
* 默认的数据库支持
* 无阻塞查询
* 支持Vault关联

## 依赖
* [SimpleORM](https://github.com/caoli5288/SimpleORM)


## 命令

* /money give \<player> \<count>
    * 给那个球员钱（
    * 需要权限： `money.give`
    * 白嫖权限： `money.admin`
* /money take \<player> \<count>
    * 从该玩家钱包里取钱。
    * 需要权限： `money.admin`
* /money look \<player>
    * 查看该玩家钱包。
    * 需要权限： `money.look`
* /money set \<player> \<count>
    * 直接设定该玩家的钱数。
    * 需要权限： `money.set`
* /money top
    * 查询排行榜前20个玩家，缓存为5分钟。
    * 需要权限： `money.top`

## 其他
* 关于浮点约数
    * 采用四舍五入约数。如默认约数两位，则0.998会被约成1.0。
    * 余额判断时不会发生约数。如判断某玩家（余额0）是否拥有0.001，则结果为假。
    * 账户操作时先对传入约数，如约后小于、等于0，则抛出异常。
    * 账户操作时以原始数进行计算，对结果约数后写入数据库。