import java.io.InputStream;
import java.util.List;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffEntry.ChangeType;
import org.eclipse.jgit.errors.LargeObjectException;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.util.IO;

	private final RevCommit commit;

	private DiffEntry diffEntry;

		List<DiffEntry> entries = DiffEntry.scan(walk);

		for (DiffEntry entry : entries) {
			final FileDiff d = new FileDiff(commit, entry);
			r.add(d);
	 * @param gitFormat
	 *            if false, do not show any source or destination prefix,
	 *            and the paths are calculated relative to the eclipse
	 *            project, otherwise relative to the git repository
			final DiffFormatter diffFmt, boolean gitFormat) throws IOException {
		if (gitFormat) {
			diffFmt.setRepository(db);
			diffFmt.format(diffEntry);
			return;
		}

		ObjectReader reader = db.newObjectReader();
		try {
			outputEclipseDiff(d, db, reader, diffFmt);
		} finally {
			reader.release();
		}
	}

	private void outputEclipseDiff(final StringBuilder d, final Repository db,
			final ObjectReader reader, final DiffFormatter diffFmt)
			throws IOException {
		if (!(getBlobs().length == 2))
		String projectRelativePath = getProjectRelativePath(db, getPath());
		d.append("diff --git ").append(projectRelativePath).append(" ") //$NON-NLS-1$ //$NON-NLS-2$
			.append(projectRelativePath).append("\n"); //$NON-NLS-1$
		final ObjectId id1 = getBlobs()[0];
		final ObjectId id2 = getBlobs()[1];
		final FileMode mode1 = getModes()[0];
		final FileMode mode2 = getModes()[1];
		d.append("index ").append(reader.abbreviate(id1).name()). //$NON-NLS-1$
				append("..").append(reader.abbreviate(id2).name()). //$NON-NLS-1$
			d.append(getProjectRelativePath(db, getPath()));
			d.append(getProjectRelativePath(db, getPath()));
		final RawText a = getRawText(id1, reader);
		final RawText b = getRawText(id2, reader);
	private String getProjectRelativePath(Repository db, String repoPath) {
	private RawText getRawText(ObjectId id, ObjectReader reader) throws IOException {
		ObjectLoader ldr = reader.open(id);
		if (!ldr.isLarge())
			return new RawText(ldr.getCachedBytes());

		long sz = ldr.getSize();
		if (Integer.MAX_VALUE <= sz)
			throw new LargeObjectException(id);

		byte[] buf = new byte[(int) sz];
		InputStream in = ldr.openStream();
		try {
			IO.readFully(in, buf, 0, buf.length);
		} finally {
			in.close();
		}
		return new RawText(buf);
	public RevCommit getCommit() {
		return commit;
	}
	public String getPath() {
		if (ChangeType.DELETE.equals(diffEntry.getChangeType()))
			return diffEntry.getOldPath();
		return diffEntry.getNewPath();
	}
	public ChangeType getChange() {
		return diffEntry.getChangeType();
	}
	public ObjectId[] getBlobs() {
		List<ObjectId> objectIds = new ArrayList<ObjectId>();
		if (diffEntry.getOldId() != null)
			objectIds.add(diffEntry.getOldId().toObjectId());
		if (diffEntry.getNewId() != null)
			objectIds.add(diffEntry.getNewId().toObjectId());
		return objectIds.toArray(new ObjectId[]{});
	}
	public FileMode[] getModes() {
		List<FileMode> modes = new ArrayList<FileMode>();
		if (diffEntry.getOldMode() != null)
			modes.add(diffEntry.getOldMode());
		if (diffEntry.getOldMode() != null)
			modes.add(diffEntry.getOldMode());
		return modes.toArray(new FileMode[]{});
	}
	FileDiff(final RevCommit c, final DiffEntry entry) {
		diffEntry = entry;