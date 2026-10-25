package net.csibio.metaphoenix.client.domain.vo;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

@Data
public class LibraryUploadVO {

    String id;
    String name;
    String type;
    Set<String> matrix;
    Set<String> species;
    Set<String> tags;
    String description;
    MultipartFile libFile;
}
