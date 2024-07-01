package com.file.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.file.service.FileService;

@RestController
public class FileController {

	@Autowired
	private FileService fileService;

	@PostMapping("/upload")
	public ResponseEntity<Void> handleFileUpload(@RequestParam("file") MultipartFile file) {
		fileService.saveFile(file);
		return ResponseEntity.status(302).header(HttpHeaders.LOCATION, "/files").build();
	}

	@GetMapping("/files")
	public List<String> listUploadedFiles() {
		return fileService.listFiles();
	}

	@GetMapping("/download")
	public ResponseEntity<Resource> serveFile(@RequestParam String filename) {
		Resource file = fileService.loadFile(filename);
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"")
				.body(file);
	}

	@DeleteMapping("/remove")
	public ResponseEntity<Void> removeFile(@RequestParam String filename) {
		fileService.deleteFile(filename);
		return ResponseEntity.ok().build();
	}
}
