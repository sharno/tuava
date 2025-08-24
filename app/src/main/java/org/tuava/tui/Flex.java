package org.tuava.tui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

public final class Flex implements Element {
	public enum Direction { ROW, COLUMN }
	public enum Justify { START, CENTER, END, SPACE_BETWEEN, SPACE_AROUND, SPACE_EVENLY }
	public enum Align { START, CENTER, END }

	private final Direction direction;
	private final Justify justify;
	private final Align align;
	private final int gap;
	private final Integer width;
	private final Integer height;
	private final List<Element> children;

	private Flex(Direction direction, Justify justify, Align align, int gap,
				 Integer width, Integer height, List<Element> children) {
		this.direction = direction;
		this.justify = justify;
		this.align = align;
		this.gap = Math.max(0, gap);
		this.width = width;
		this.height = height;
		this.children = List.copyOf(children);
	}

	public static Builder of() { return new Builder(); }
	public static Builder row() { return new Builder().direction(Direction.ROW); }
	public static Builder column() { return new Builder().direction(Direction.COLUMN); }

	@Override
	public String render() {
		return switch (direction) {
			case COLUMN -> renderColumn();
			case ROW -> renderRow();
		};
	}

	private String renderColumn() {
		List<String[]> blocks = renderChildrenAsBlocks();
		int containerWidth = width != null ? width : computeMaxWidth(blocks);
		int contentHeight = sumHeights(blocks) + gap * Math.max(0, blocks.size() - 1);
		int containerHeight = height != null ? Math.max(height, 0) : contentHeight;

		int remaining = Math.max(0, containerHeight - contentHeight);
		int topPad = 0, bottomPad = 0;
		int[] betweenExtra = new int[Math.max(0, blocks.size() - 1)];

		switch (justify) {
			case START -> bottomPad = remaining;
			case END -> topPad = remaining;
			case CENTER -> {
				topPad = remaining / 2;
				bottomPad = remaining - topPad;
			}
			case SPACE_BETWEEN -> {
				int slots = Math.max(0, blocks.size() - 1);
				if (slots > 0) {
					int per = remaining / slots;
					int rem = remaining % slots;
					for (int i = 0; i < slots; i++) {
						betweenExtra[i] = per + (i < rem ? 1 : 0);
					}
				}
			}
			case SPACE_AROUND -> {
				int slots = blocks.size() + 1;
				int per = remaining / slots;
				int rem = remaining % slots;
				topPad = per + (rem > 0 ? 1 : 0);
				bottomPad = per;
				int extra = per;
				for (int i = 0; i < betweenExtra.length; i++) betweenExtra[i] = extra;
			}
			case SPACE_EVENLY -> {
				int slots = blocks.size() + 1;
				int per = slots == 0 ? 0 : remaining / slots;
				topPad = per;
				bottomPad = per;
				for (int i = 0; i < betweenExtra.length; i++) betweenExtra[i] = per;
			}
		}

		List<String> lines = new ArrayList<>();
		// Top padding
		for (int i = 0; i < topPad; i++) lines.add(padLine("", containerWidth));

		for (int b = 0; b < blocks.size(); b++) {
			String[] block = blocks.get(b);
			for (String line : block) {
				lines.add(alignHorizontally(line, containerWidth));
			}
			if (b < blocks.size() - 1) {
				int gapLines = gap + betweenExtra[b];
				for (int g = 0; g < gapLines; g++) lines.add(padLine("", containerWidth));
			}
		}

		// Bottom padding
		for (int i = 0; i < bottomPad; i++) lines.add(padLine("", containerWidth));

		return String.join("\n", lines);
	}

	private String renderRow() {
		List<String[]> rawBlocks = renderChildrenAsBlocks();
		int baseGap = gap;

		int contentHeight = computeMaxHeight(rawBlocks);
		int containerHeight = height != null ? Math.max(height, 0) : contentHeight;

		// Align each block vertically to containerHeight
		List<String[]> blocks = new ArrayList<>(rawBlocks.size());
		int[] blockWidths = new int[rawBlocks.size()];
		for (int i = 0; i < rawBlocks.size(); i++) {
			String[] block = rawBlocks.get(i);
			String[] aligned = alignVertically(block, containerHeight);
			blocks.add(aligned);
			blockWidths[i] = computeMaxWidth(Collections.singletonList(aligned));
		}

		int childrenWidth = 0;
		for (int w : blockWidths) childrenWidth += w;
		int baseGapsWidth = baseGap * Math.max(0, blocks.size() - 1);

		int containerWidth = width != null ? Math.max(width, 0) : childrenWidth + baseGapsWidth;
		int remaining = Math.max(0, containerWidth - (childrenWidth + baseGapsWidth));

		int leftPad = 0, rightPad = 0;
		int[] betweenExtra = new int[Math.max(0, blocks.size() - 1)];

		switch (justify) {
			case START -> rightPad = remaining;
			case END -> leftPad = remaining;
			case CENTER -> {
				leftPad = remaining / 2;
				rightPad = remaining - leftPad;
			}
			case SPACE_BETWEEN -> {
				int slots = Math.max(0, blocks.size() - 1);
				if (slots > 0) {
					int per = remaining / slots;
					int rem = remaining % slots;
					for (int i = 0; i < slots; i++) betweenExtra[i] = per + (i < rem ? 1 : 0);
				}
			}
			case SPACE_AROUND -> {
				int slots = blocks.size() + 1;
				int per = remaining / slots;
				int rem = remaining % slots;
				leftPad = per + (rem > 0 ? 1 : 0);
				rightPad = per;
				for (int i = 0; i < betweenExtra.length; i++) betweenExtra[i] = per;
			}
			case SPACE_EVENLY -> {
				int slots = blocks.size() + 1;
				int per = slots == 0 ? 0 : remaining / slots;
				leftPad = per;
				rightPad = per;
				for (int i = 0; i < betweenExtra.length; i++) betweenExtra[i] = per;
			}
		}

		List<String> lines = new ArrayList<>(containerHeight);
		for (int row = 0; row < containerHeight; row++) {
			StringBuilder sb = new StringBuilder();
			sb.append(" ".repeat(Math.max(0, leftPad)));
			for (int i = 0; i < blocks.size(); i++) {
				String[] block = blocks.get(i);
				String line = row < block.length ? block[row] : "";
				int padRightTo = blockWidths[i];
				sb.append(padRightVisible(line, padRightTo));
				if (i < blocks.size() - 1) {
					int spaces = baseGap + betweenExtra[i];
					sb.append(" ".repeat(Math.max(0, spaces)));
				}
			}
			sb.append(" ".repeat(Math.max(0, rightPad)));
			String full = sb.toString();
			if (full.length() < containerWidth) {
				full = padRightVisible(full, containerWidth);
			}
			lines.add(full);
		}

		return String.join("\n", lines);
	}

	private List<String[]> renderChildrenAsBlocks() {
		List<String[]> blocks = new ArrayList<>(children.size());
		for (Element e : children) {
			String rendered = e == null ? "" : e.render();
			blocks.add(rendered.split("\n", -1));
		}
		return blocks;
	}

	private static int sumHeights(List<String[]> blocks) {
		int h = 0;
		for (String[] b : blocks) h += b.length;
		return h;
	}

	private static int computeMaxHeight(List<String[]> blocks) {
		int h = 0;
		for (String[] b : blocks) h = Math.max(h, b.length);
		return h;
	}

	private static int computeMaxWidth(List<String[]> blocks) {
		int w = 0;
		for (String[] b : blocks) {
			for (String s : b) w = Math.max(w, visibleLength(s));
		}
		return w;
	}

	private String[] alignVertically(String[] block, int targetHeight) {
		if (block.length >= targetHeight) return block;
		int remaining = targetHeight - block.length;
		int top = 0, bottom = 0;
		switch (align) {
			case START -> bottom = remaining;
			case END -> top = remaining;
			case CENTER -> {
				top = remaining / 2;
				bottom = remaining - top;
			}
		}
		List<String> lines = new ArrayList<>(targetHeight);
		for (int i = 0; i < top; i++) lines.add("");
		for (String s : block) lines.add(s);
		for (int i = 0; i < bottom; i++) lines.add("");
		return lines.toArray(new String[0]);
	}

	private String alignHorizontally(String line, int containerWidth) {
		return switch (align) {
			case START -> Layout.padRight(line, containerWidth);
			case END -> Layout.padLeft(line, containerWidth);
			case CENTER -> Layout.center(line, containerWidth);
		};
	}

	private static final Pattern ANSI_PATTERN = Pattern.compile("\\u001B\\[[0-9;]*m");
	private static int visibleLength(String text) {
		if (text == null || text.isEmpty()) return 0;
		return ANSI_PATTERN.matcher(text).replaceAll("").length();
	}

	private static String padRightVisible(String text, int width) {
		int vis = visibleLength(text);
		if (vis >= width) return text;
		return text + " ".repeat(width - vis);
	}

	private static String padLine(String text, int width) {
		return padRightVisible(text, width);
	}

	public static final class Builder {
		private Direction direction = Direction.COLUMN;
		private Justify justify = Justify.START;
		private Align align = Align.START;
		private int gap = 0;
		private Integer width;
		private Integer height;
		private final List<Element> children = new ArrayList<>();

		public Builder direction(Direction direction) { this.direction = Objects.requireNonNull(direction); return this; }
		public Builder justify(Justify justify) { this.justify = Objects.requireNonNull(justify); return this; }
		public Builder align(Align align) { this.align = Objects.requireNonNull(align); return this; }
		public Builder gap(int gap) { this.gap = Math.max(0, gap); return this; }
		public Builder width(int width) { this.width = width; return this; }
		public Builder height(int height) { this.height = height; return this; }
		public Builder children(List<? extends Element> elements) { this.children.clear(); this.children.addAll(elements); return this; }
		public Builder add(Element element) { this.children.add(Objects.requireNonNull(element)); return this; }

		public Flex build() { return new Flex(direction, justify, align, gap, width, height, children); }
	}
}


