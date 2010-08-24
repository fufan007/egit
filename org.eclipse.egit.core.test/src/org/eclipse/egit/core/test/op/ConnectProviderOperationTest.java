/*******************************************************************************
 * Copyright (C) 2007, Robin Rosenberg <robin.rosenberg@dewire.com>
 * Copyright (C) 2008, Shawn O. Pearce <spearce@spearce.org>
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.egit.core.test.op;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.TimeZone;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.egit.core.op.ConnectProviderOperation;
import org.eclipse.egit.core.test.GitTestCase;
import org.eclipse.jgit.lib.CommitBuilder;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.FileTreeEntry;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectInserter;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.RefUpdate;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.Tree;
import org.eclipse.jgit.storage.file.FileRepository;
import org.eclipse.team.core.RepositoryProvider;
import org.junit.Test;

public class ConnectProviderOperationTest extends GitTestCase {

	@Test
	public void testNoRepository() throws CoreException {

		ConnectProviderOperation operation = new ConnectProviderOperation(
				project.getProject(), new File("../..", Constants.DOT_GIT));
		operation.execute(null);

		assertFalse(RepositoryProvider.isShared(project.getProject()));
		assertTrue(!gitDir.exists());
	}

	@Test
	public void testNewRepository() throws CoreException, IOException {

		Repository repository = new FileRepository(gitDir);
		repository.create();
		repository.close();
		ConnectProviderOperation operation = new ConnectProviderOperation(
				project.getProject(), gitDir);
		operation.execute(null);

		assertTrue(RepositoryProvider.isShared(project.getProject()));

		assertTrue(gitDir.exists());
	}

	@Test
	public void testNewUnsharedFile() throws CoreException, IOException {

		project.createSourceFolder();
		IFile fileA = project.getProject().getFolder("src").getFile("A.java");
		String srcA = "class A {\n" + "}\n";
		fileA.create(new ByteArrayInputStream(srcA.getBytes("UTF-8")), false, null);

		Repository thisGit = new FileRepository(gitDir);
		thisGit.create();
		Tree rootTree = new Tree(thisGit);
		Tree prjTree = rootTree.addTree(project.getProject().getName());
		Tree srcTree = prjTree.addTree("src");
		FileTreeEntry entryA = srcTree.addFile("A.java");

		ObjectId id;
		ObjectInserter inserter = thisGit.newObjectInserter();
		try {
			entryA.setId(inserter.insert(Constants.OBJ_BLOB, srcA.getBytes("UTF-8")));
			srcTree.setId(inserter.insert(Constants.OBJ_TREE, srcTree.format()));
			prjTree.setId(inserter.insert(Constants.OBJ_TREE, prjTree.format()));
			rootTree.setId(inserter.insert(Constants.OBJ_TREE, rootTree.format()));
			CommitBuilder commit = new CommitBuilder();
			commit.setTreeId(rootTree.getTreeId());
			commit.setAuthor(new PersonIdent("J. Git", "j.git@egit.org",
					new Date(60876075600000L), TimeZone.getTimeZone("GMT+1")));
			commit.setCommitter(commit.getAuthor());
			commit.setMessage("testNewUnsharedFile\n\nJunit tests\n");
			id = inserter.insert(commit);
			inserter.flush();
		} finally {
			inserter.release();
		}

		RefUpdate lck = thisGit.updateRef("refs/heads/master");
		assertNotNull("obtained lock", lck);
		lck.setNewObjectId(id);
		assertEquals(RefUpdate.Result.NEW, lck.forceUpdate());

		ConnectProviderOperation operation = new ConnectProviderOperation(
				project.getProject(), gitDir);
		operation.execute(null);

		assertNotNull(RepositoryProvider.getProvider(project.getProject()));

	}
}
