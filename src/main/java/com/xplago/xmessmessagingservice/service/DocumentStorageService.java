package com.xplago.xmessmessagingservice.service;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.xplago.xmessmessagingservice.dto.document.File;
import com.xplago.xmessmessagingservice.exception.CustomException;
import com.xplago.xmessmessagingservice.exception.DocumentNotFoundException;
import com.xplago.xmessmessagingservice.model.chat.XMessChat;
import com.xplago.xmessmessagingservice.model.document.Document;
import com.xplago.xmessmessagingservice.model.document.DocumentAccess;
import com.xplago.xmessmessagingservice.repository.XMessChatRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsOperations;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

@Service
public class DocumentStorageService {

    @Autowired
    private GridFsTemplate gridFsTemplate;

    @Autowired
    private GridFsOperations operations;

    @Autowired
    private XMessChatRepository chatRepository;

    public String store(MultipartFile file, String chatId, DocumentAccess access) throws IOException {
        DBObject metaData = new BasicDBObject();
        metaData.put("type", file.getContentType());
        metaData.put("title", file.getOriginalFilename());
        metaData.put("access", access);
        if (chatId != null) metaData.put("chatId", chatId);
        if (chatId != null) {
            metaData.put("chatId", chatId);
            ObjectId id = gridFsTemplate.store(file.getInputStream(), file.getOriginalFilename(), file.getContentType(), metaData);
            Optional<XMessChat> chatOptional = chatRepository.findById(chatId);
            if (chatOptional.isPresent()) {
                XMessChat chat = chatOptional.get();
                chat.getDocuments().add(id.toString());
                chatRepository.save(chat);
                return id.toString();
            } else {
                throw new CustomException("Chat is not found", HttpStatus.NOT_FOUND);
            }
        } else {
            ObjectId id = gridFsTemplate.store(file.getInputStream(), file.getOriginalFilename(), file.getContentType(), metaData);
            return id.toString();
        }
    }

    public String store(InputStream is, String name, String contentType, String chatId, DocumentAccess access) throws IOException {
        DBObject metaData = new BasicDBObject();
        metaData.put("type", contentType);
        metaData.put("title", name);
        metaData.put("access", access.toString());
        if (chatId != null) {
            metaData.put("chatId", chatId);
            ObjectId id = gridFsTemplate.store(is, name, contentType, metaData);
            Optional<XMessChat> chatOptional = chatRepository.findById(chatId);
            if (chatOptional.isPresent()) {
                XMessChat chat = chatOptional.get();
                chat.getDocuments().add(id.toString());
                chatRepository.save(chat);
                return id.toString();
            } else {
                throw new CustomException("Chat is not found", HttpStatus.NOT_FOUND);
            }
        } else {
            ObjectId id = gridFsTemplate.store(is, name, contentType, metaData);
            return id.toString();
        }
    }

    public Document getDocument(String id) throws IOException, IllegalStateException {
        GridFSFile file = gridFsTemplate.findOne(new Query(Criteria.where("_id").is(id)));
        if (file == null) throw new DocumentNotFoundException("File with id: \"" + id + "\" not found");
        Document document = new Document();

        document.setName(file.getMetadata().get("title").toString());

        Object chatId = file.getMetadata().get("chatId");
        if (chatId != null) document.setChatId(chatId.toString());

        Object access = file.getMetadata().get("access");
        if (access != null) document.setAccess(DocumentAccess.valueOf(access.toString()));

        document.setData(operations.getResource(file).getInputStream());

        return document;
    }

    public File getFile(String id, String chatId) {
        GridFSFile file = gridFsTemplate.findOne(new Query(Criteria.where("_id").is(id)));
        if (file == null) throw new DocumentNotFoundException("File with id: \"" + id + "\" not found");
        File f = new File();

        f.setId(id);
        f.setName(file.getMetadata().get("title").toString());

        Object access = file.getMetadata().get("access");
        if (access != null) f.setAccess(DocumentAccess.valueOf(access.toString()));

        Object cId = file.getMetadata().get("chatId");
        if (f.getAccess() == DocumentAccess.CHAT) {
            if (cId != null && chatId == cId) f.setChatId(cId.toString());
        } else {
            if (cId != null) f.setChatId(cId.toString());
        }

        return f;
    }
}
