package org.devgateway.toolkit.forms.service;

import com.mongodb.client.gridfs.model.GridFSFile;
import org.bson.types.ObjectId;
import org.devgateway.ocds.persistence.mongo.Document;
import org.devgateway.toolkit.persistence.dao.FileMetadata;

import java.net.URI;

public interface MongoFileStorageService {

    Document storeFileAndReferenceAsDocument(FileMetadata fm, Document.DocumentType documentType);

    GridFSFile retrieveFile(ObjectId id);

    URI createURL(ObjectId id);

}
