package Models

class Banner {
    var productimage:String?=null
    var bannername:String?=null
    var itemid:String?=null

    constructor()
    constructor(bannername:String,itemid:String,productimage:String)
    {
        this.bannername=bannername
        this.itemid=itemid
        this.productimage=productimage
    }
}