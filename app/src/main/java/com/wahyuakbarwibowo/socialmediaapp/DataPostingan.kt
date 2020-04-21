package com.wahyuakbarwibowo.socialmediaapp

class DataPostingan {
    var postID: String? = null
    var postText: String? = null
    var postImageURL: String? = null
    var postPersonUID: String? = null
    constructor(postIDs: String, postTexts: String, postImageURLs: String, postPersonUIDs: String){
        this.postID = postIDs
        this.postPersonUID = postPersonUIDs
        this.postText = postTexts
        this.postImageURL = postImageURLs
    }
}