package com.wahyuakbarwibowo.socialmediaapp

class InfoPostingan {
    var UserID: String? = null
    var text: String? = null
    var postImage: String? = null
    constructor(UserID: String, text: String, postImage: String){
        this.UserID = UserID
        this.text = text
        this.postImage = postImage
    }
}