package com.findwise.hydra.output.elasticsearch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.elasticsearch.ElasticSearchException;
import org.elasticsearch.action.ListenableActionFuture;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.XContentType;

import com.findwise.hydra.common.Document.Action;
import com.findwise.hydra.common.Logger;
import com.findwise.hydra.local.LocalDocument;
import com.findwise.hydra.stage.AbstractOutputStage;
import com.findwise.hydra.stage.Parameter;
import com.findwise.hydra.stage.RequiredArgumentMissingException;
import com.findwise.hydra.stage.Stage;

@Stage(description = "A stage that writes documents to elasticsearch")
public class ElasticsearchOutputStage extends AbstractOutputStage {

	@Parameter(description = "List of elasticsearch nodes to connect to")
	private List<String> esNodes = new ArrayList<String>();

	@Parameter(description = "Transport port of the elasticsearch nodes")
	private int transportPort = 9300;
	
	@Parameter(description = "Name of the cluster to join")
	private String clusterName = "elasticsearch";
	
	@Parameter(description = "Name of elasticsearch index")
	private String documentIndex = "main";
	
	@Parameter(description = "Document type")
	private String documentType = "default";
	
	private Client client;
	
	@Override
	public void init() throws RequiredArgumentMissingException {
		client = constructClient();
	}
	
	@Override
	public void output(LocalDocument document) {
		final Action action = Action.ADD;//document.getAction();
		
		try {
			Logger.debug(action.toString());
			if (action == Action.ADD) {
				add(document);
			}
			else if (action == Action.DELETE) {
				delete(document);
			}
		} catch (ElasticSearchException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	private void add(LocalDocument document) throws ElasticSearchException, IOException {
		//String docId = (String)document.getContentField("docId");
		String json = document.contentFieldsToJson(document.getContentFields());
		Logger.debug("Indexing document to index " + documentIndex + " with type " + documentType);
		ListenableActionFuture<IndexResponse> actionFuture = client.prepareIndex(documentIndex, documentType)
			.setSource(json)
			.execute();
		IndexResponse response = actionFuture.actionGet(1000);
		Logger.debug("Got response for docId " + response.getId());
	}
	
	private void delete(LocalDocument document) {
		
	}

	private Client constructClient() {
		Settings settings = ImmutableSettings.settingsBuilder()
				.put("cluster.name", clusterName)
				.build();
		TransportClient tclient = new TransportClient(settings);
		for (String node : esNodes) {
			tclient.addTransportAddress(new InetSocketTransportAddress(node, transportPort));
		}
		return tclient;
	}
	
	public void setClient(Client client) {
		this.client = client;
	}
	
	public String getClusterName() {
		return clusterName;
	}

	public void setClusterName(String clusterName) {
		this.clusterName = clusterName;
	}

	public List<String> getEsNodes() {
		return esNodes;
	}

	public void setEsNodes(List<String> esNodes) {
		this.esNodes = esNodes;
	}
}
