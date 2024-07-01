package com.file.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.file.entity.FileEntity;

public interface FileRepository extends JpaRepository<FileEntity, Long> {

}
