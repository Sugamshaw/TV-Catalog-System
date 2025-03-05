package Models

import java.io.Serializable

data class Catalog(
    var catalogname: String? = null,
    var itemid: String? = null,
    var priceperkg: String? = null,
    var producttitle: String? = null,
    var productimage: String? = null,
    var productdescription: String? = null,
    var mrp: String? = null,
    var catalogstatus: String? = null
) : Serializable

//class Catalog {
//    var catalogname:String?=null
//    var itemid:String?=null
//    var priceperkg:String?=null
//    var producttitle:String?=null
//    var productimage:String?=null
//    var productdescription:String?=null
//    var mrp:String?=null
//    var catalogstatus:String?=null
//    constructor()
//
//    constructor(catalogname:String,itemid:String,priceperkg:String,producttitle:String,productimage:String,productdescription:String,mrp:String,catalogstatus:String)
//    {
//        this.catalogname=catalogname
//        this.itemid=itemid
//        this.priceperkg=priceperkg
//        this.producttitle=producttitle
//        this.productimage=productimage
//        this.productdescription=productdescription
//        this.mrp=mrp
//        this.catalogstatus=catalogstatus
//    }
//}


