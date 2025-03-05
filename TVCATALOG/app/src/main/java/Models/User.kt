package Models

class User {

    var image:String?=null
    var storename:String?=null
    var email:String?=null
    var password:String?=null

    constructor()

    constructor(image:String?,storename:String?,email:String?,password:String?)
    {
        this.image=image
        this.storename=storename
        this.email=email
        this.password=password
    }
    constructor(storename:String?,email:String?,password:String?)
    {
        this.storename=storename
        this.email=email
        this.password=password
    }

    constructor(email:String?,password: String?)
    {

        this.email=email
        this.password=password
    }

}