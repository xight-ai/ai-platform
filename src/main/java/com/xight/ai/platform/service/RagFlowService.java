package com.xight.ai.platform.service;

import com.xight.ai.platform.model.ragflow.DocPageResult;
import com.xight.ai.platform.model.ragflow.RagflowDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RagFlowService {

    @Autowired
    private RagFlowHttpApi ragFlowHttpApi;

    @Value("${ragflow.pageSize}")
    private int pageSize;

    public List<RagflowDocument> searchDocuments(String datasetId, String documentId, String documentName) {
        List<RagflowDocument> documents = new ArrayList<>();
        int page = 1;
        int total;
        do {
            DocPageResult docPageResult = ragFlowHttpApi.listDocuments(datasetId, page, documentId, documentName).getData();
            if (docPageResult != null) {
                documents.addAll(docPageResult.getDocs());
                total = docPageResult.getTotal();
                page++;
            } else {
                total = 0;
            }

        } while ((page - 1) * pageSize < total);
        return documents;
    }

}
