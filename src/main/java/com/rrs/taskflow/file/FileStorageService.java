package com.rrs.taskflow.file;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class FileStorageService {

    @Autowired
    private Cloudinary cloudinary;

    // =====================================================
    // SAVE PROFILE IMAGE
    // =====================================================
    // uploads image to cloudinary and returns permanent url
    // url is stored in db — never changes even after restart
    // =====================================================
    public String saveProfileImage(MultipartFile file) throws IOException {

        // Step 1: validate file before uploading
        validateImageFile(file);

        // Step 2: upload file to cloudinary
        // folder = "taskflow/profiles" — organizes images in cloudinary dashboard
        Map uploadResult = cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap(
                        "folder", "taskflow/profiles",
                        "resource_type", "image"
                )
        );

        // Step 3: return permanent cloud url
        // ex: https://res.cloudinary.com/yourname/image/upload/taskflow/profiles/photo.jpg
        return uploadResult.get("secure_url").toString();
    }

    // =====================================================
    // DELETE PROFILE IMAGE
    // =====================================================
    // deletes image from cloudinary using public id
    // public id is extracted from the stored url
    // =====================================================
    public void deleteProfileImage(String imageUrl) {

        try {
            // extract public id from url
            // ex url: https://res.cloudinary.com/demo/image/upload/taskflow/profiles/abc123.jpg
            // ex public id: taskflow/profiles/abc123
            String publicId = extractPublicId(imageUrl);

            // delete from cloudinary
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());

        } catch (IOException e) {
            // log error but dont crash app
            // old image cleanup failure should not block user update
            System.out.println("Could not delete old image from cloudinary: " + e.getMessage());
        }
    }

    // =====================================================
    // VALIDATE IMAGE FILE
    // =====================================================
    // checks file is not empty and is a valid image type
    // =====================================================
    private void validateImageFile(MultipartFile file) {

        // check file is not empty
        if (file.isEmpty()) {
            throw new RuntimeException("File is empty. Please upload a valid image.");
        }

        String originalFileName = file.getOriginalFilename();

        if (originalFileName == null) {
            throw new RuntimeException("Invalid file. Please upload a valid image.");
        }

        // extract file extension
        String fileExtension = originalFileName
                .substring(originalFileName.lastIndexOf(".") + 1)
                .toLowerCase();

        // only allow jpg, jpeg, png, webp
        if (!fileExtension.equals("jpg") &&
                !fileExtension.equals("jpeg") &&
                !fileExtension.equals("png") &&
                !fileExtension.equals("webp")) {

            throw new RuntimeException("Invalid file type. Only JPG, JPEG, PNG and WEBP allowed.");
        }
    }

    // =====================================================
    // EXTRACT PUBLIC ID FROM URL
    // =====================================================
    // cloudinary needs public id to delete image
    // public id is the path after /upload/ in the url
    // =====================================================
    private String extractPublicId(String imageUrl) {

        // ex url: https://res.cloudinary.com/demo/image/upload/v123456/taskflow/profiles/abc.jpg
        // we need: taskflow/profiles/abc

        // split by /upload/
        String afterUpload = imageUrl.split("/upload/")[1];

        // remove version number if present (v123456/)
        if (afterUpload.startsWith("v")) {
            afterUpload = afterUpload.substring(afterUpload.indexOf("/") + 1);
        }

        // remove file extension (.jpg, .png etc)
        return afterUpload.substring(0, afterUpload.lastIndexOf("."));
    }
}