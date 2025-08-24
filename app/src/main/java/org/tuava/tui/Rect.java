package org.tuava.tui;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class Rect implements Element {
	private final int width;
	private final int height;
	private final List<Element> children;
	private final boolean boxed;

	private Rect(int width, int height, List<Element> children, boolean boxed) {
		this.width = width;
		this.height = height;
		this.children = List.copyOf(children);
		this.boxed = boxed;
	}

	public static Builder width(int width) {
		return new Builder().width(width);
	}

	public static Builder size(int width, int height) {
		return new Builder().width(width).height(height);
	}

	public static Builder of() {
		return new Builder();
	}

	@Override
	public String render() {
		String content = Layout.verticalJoin(children.stream().map(Element::render).toList());
		if (boxed) {
			int w = Math.max(2, width);
			int h = Math.max(2, height);
			return Layout.box(content, w, h);
		}
		return content;
	}

	public static final class Builder {
		private Integer width;
		private Integer height;
		private final List<Element> children = new ArrayList<>();
		private boolean boxed = false;

		public Builder width(int width) {
			this.width = width;
			return this;
		}

		public Builder height(int height) {
			this.height = height;
			return this;
		}

		public Builder boxed() {
			this.boxed = true;
			return this;
		}

		public Builder children(List<? extends Element> elements) {
			this.children.clear();
			this.children.addAll(elements);
			return this;
		}

		public Builder add(Element element) {
			this.children.add(Objects.requireNonNull(element));
			return this;
		}

		public Rect build(List<? extends Element> children) {
			this.children(children);
			return build();
		}

		public Rect build() {
			int w = this.width == null ? 0 : this.width;
			int h = this.height == null ? 0 : this.height;
			return new Rect(w, h, this.children, this.boxed);
		}
	}
}


