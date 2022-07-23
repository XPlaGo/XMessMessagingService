package com.xplago.xmessmessagingservice.controller;

import com.xplago.xmessmessagingservice.dto.document.DocumentRequest;
import com.xplago.xmessmessagingservice.exception.CustomException;
import com.xplago.xmessmessagingservice.model.chat.XMessChat;
import com.xplago.xmessmessagingservice.model.document.Document;
import com.xplago.xmessmessagingservice.model.document.DocumentAccess;
import com.xplago.xmessmessagingservice.model.user.XMessUser;
import com.xplago.xmessmessagingservice.repository.XMessChatRepository;
import com.xplago.xmessmessagingservice.repository.XMessUserRepository;
import com.xplago.xmessmessagingservice.service.DocumentStorageService;
import com.xplago.xmessmessagingservice.service.XMessChatService;
import com.xplago.xmessmessagingservice.service.XMessMessageService;
import com.xplago.xmessmessagingservice.service.XMessUserService;
import org.apache.commons.io.FilenameUtils;
import org.imgscalr.Scalr;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.*;
import java.util.Objects;

@RestController
@RequestMapping("/document")
@CrossOrigin(origins = {"http://localhost:3000", "https://xmess.jelastic.regruhosting.ru"})
public class DocumentController {
    @Autowired
    private DocumentStorageService documentStorageService;

    @Autowired
    private XMessUserService userService;

    @Autowired
    private XMessUserRepository userRepository;

    @Autowired
    private XMessChatRepository chatRepository;

    @Autowired
    private XMessChatService chatService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private XMessMessageService messageService;

    @GetMapping("/{id}")
    public void getDocument(@PathVariable String id, HttpServletResponse response) throws IOException {
        Document document = documentStorageService.getDocument(id);
        if (document.getAccess() == DocumentAccess.ALL)
            FileCopyUtils.copy(document.getData(), response.getOutputStream());
        else {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            XMessUser user = userService.requestUserFromUsername(username);
            for (XMessChat chat : user.getChats()) {
                if (chat.getId().equals(document.getChatId()))
                    FileCopyUtils.copy(document.getData(), response.getOutputStream());
            }
            throw new CustomException("You do not have access rights to this file", HttpStatus.FORBIDDEN);
        }
    }

    @PostMapping("/uploadWithMessage")
    public String uploadDocumentWithMessage(@RequestParam("file") MultipartFile file, @RequestHeader("chatId") String chatId) throws IOException {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        XMessUser user = userService.requestUserFromUsername(username);

        for (XMessChat chat : user.getChats())
            if (chat.getId().equals(chatId))
                return documentStorageService.store(file, chatId, DocumentAccess.ALL);
        throw new CustomException("The chat does not exist or you do not have access to this chat", HttpStatus.FORBIDDEN);
    }

    @PostMapping("/avatar")
    public String uploadAvatar(@RequestParam("file") MultipartFile file) throws IOException {
        BufferedImage bufferedImage = ImageIO.read(file.getInputStream());
        int height = bufferedImage.getHeight();
        int width = bufferedImage.getWidth();
        BufferedImage resized;
        if (height < width) {
            resized = Scalr.resize(bufferedImage, (int) (100 * ((double) width / (double) height)), 100);
        } else {
            resized = Scalr.resize(bufferedImage, 100, (int) (100 * ((double) height / (double) width)));
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(resized, FilenameUtils.getExtension(file.getOriginalFilename()), baos);
        resized.flush();

        String avatarId = documentStorageService.store(new ByteArrayInputStream(baos.toByteArray()), file.getOriginalFilename(), file.getContentType(), null, DocumentAccess.ALL);
        XMessUser user = userService.requestUserFromUsername(SecurityContextHolder.getContext().getAuthentication().getName());
        user.setAvatarId(avatarId);
        userRepository.save(user);
        return avatarId;
    }

    @PostMapping("/chatAvatar")
    public String uploadChatAvatar(@RequestParam("file") MultipartFile file, @RequestHeader("chatId") String chatId) throws IOException {
        BufferedImage bufferedImage = ImageIO.read(file.getInputStream());
        int height = bufferedImage.getHeight();
        int width = bufferedImage.getWidth();
        BufferedImage resized;
        if (height < width) {
            resized = Scalr.resize(bufferedImage, (int) (100 * ((double) width / (double) height)), 100);
        } else {
            resized = Scalr.resize(bufferedImage, 100, (int) (100 * ((double) height / (double) width)));
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(resized, FilenameUtils.getExtension(file.getOriginalFilename()), baos);
        resized.flush();

        String avatarId = documentStorageService.store(new ByteArrayInputStream(baos.toByteArray()), file.getOriginalFilename(), file.getContentType(), null, DocumentAccess.ALL);
        XMessUser user = userService.requestUserFromUsername(SecurityContextHolder.getContext().getAuthentication().getName());
        for (XMessChat chat : user.getChats()) {
            if (Objects.equals(chat.getId(), chatId)) {
                chat.setAvatarId(avatarId);
                chatRepository.save(chat);

                messageService.createAndSendInfoMessage(chatId, user.getUsername() + " changed chat avatar", "/update/messages/" + chatId);

                return avatarId;
            }
        }
        return null;
    }
}

//anonymousUser