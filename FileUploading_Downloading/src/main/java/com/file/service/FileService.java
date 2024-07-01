package com.file.service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.file.configuration.FileStorageProperties;
import com.file.entity.FileEntity;
import com.file.repo.FileRepository;

import jakarta.annotation.PostConstruct;

@Service
public class FileService {

	private final Path rootLocation;

	@Autowired
	private FileRepository fileMetadataRepository;

	public FileService(FileStorageProperties properties) {
		this.rootLocation = Paths.get(properties.getUploadDir());
	}

	@PostConstruct
	public void init() {
		try {
			Files.createDirectories(rootLocation);
		} catch (IOException e) {
			throw new RuntimeException("Could not initialize storage", e);
		}
	}

	public void saveFile(MultipartFile file) {
		try {
			if (file.isEmpty()) {
				throw new RuntimeException("Failed to store empty file.");
			}
			Path destinationFile = this.rootLocation.resolve(Paths.get(file.getOriginalFilename())).normalize()
					.toAbsolutePath();
			Files.copy(file.getInputStream(), destinationFile, StandardCopyOption.REPLACE_EXISTING);

			FileEntity fileMetadata = new FileEntity();
			fileMetadata.setFileName(file.getOriginalFilename());
			fileMetadata.setFilePath(destinationFile.toString());
			fileMetadataRepository.save(fileMetadata);
		} catch (IOException e) {
			throw new RuntimeException("Failed to store file.", e);
		}
	}

	public List<String> listFiles() {
		return fileMetadataRepository.findAll().stream().map(FileEntity::getFileName).collect(Collectors.toList());
	}

	public Resource loadFile(String filename) {
		try {
			FileEntity fileMetadata = fileMetadataRepository.findAll().stream()
					.filter(file -> file.getFileName().equals(filename)).findFirst()
					.orElseThrow(() -> new RuntimeException("File not found: " + filename));

			Path file = Paths.get(fileMetadata.getFilePath());
			Resource resource = new UrlResource(file.toUri());
			if (resource.exists() || resource.isReadable()) {
				return resource;
			} else {
				throw new RuntimeException("Could not read file: " + filename);
			}
		} catch (MalformedURLException e) {
			throw new RuntimeException("Could not read file: " + filename, e);
		}
	}

	public void deleteFile(String filename) {
		try {
			FileEntity fileMetadata = fileMetadataRepository.findAll().stream()
					.filter(file -> file.getFileName().equals(filename)).findFirst()
					.orElseThrow(() -> new RuntimeException("File not found: " + filename));

			Path file = Paths.get(fileMetadata.getFilePath());
			Files.deleteIfExists(file);
			fileMetadataRepository.delete(fileMetadata);
		} catch (IOException e) {
			throw new RuntimeException("Failed to delete file.", e);
		}
	}
}
