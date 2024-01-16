package com.pim.server.beans;

import lombok.Data;

@Data
public class Author {

    /**
     * "author": {
     *       "firstName": "Janice",
     *       "id": "e52552f4-835d-4dbe-ba77-b076e659774d",
     *       "imageUrl": "https://i.pravatar.cc/300?u=e52552f4-835d-4dbe-ba77-b076e659774d",
     *       "lastName": "King"
     *     },
     */

    String firstName = "";
    String id = "";
    String imageUrl = "";
    String lastName = "";

}
