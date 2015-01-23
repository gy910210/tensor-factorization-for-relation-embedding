package SceneList_Generate;


import java.io.*;
import java.util.*;

public class SceneTop
{
	private static final String CORPUS_PATH = "data/corpus/";
	private static final String RAW_SCENE_PATH = "data/scene/raw_scene.txt";
	private static final String FILTERED_SCENE_PATH = "data/scene/filtered_scene.txt";
	private static final String SCENE_LIST_PATH = "data/scene/scene_list_nn.txt";
	private static final String SCENE_WN_PATH = "data/scene/";
	private static final String SCENE_PB_PATH = "data/scene/";
	
	public static void Implementation() throws Exception
	{
		//SceneGrab.Implementation( CORPUS_PATH, RAW_SCENE_PATH );
		//SceneFilter.Implementation( RAW_SCENE_PATH, FILTERED_SCENE_PATH );
		//SceneGroup.Implementation( FILTERED_SCENE_PATH, SCENE_LIST_PATH );
		//SceneKB.Implementation_WN(SCENE_WN_PATH);
		//SceneKB.Implementation_PB(SCENE_PB_PATH);
		SceneKB.Intersection(SCENE_LIST_PATH, SCENE_WN_PATH+"scene_wn.txt", SCENE_WN_PATH + "scene_intersected.txt");
	}
	
}