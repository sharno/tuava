package org.tuava.tui;

import java.util.Objects;

public final class Text implements Element {
	private final String content;
	private final Style style;

	private Text(String content, Style style) {
		this.content = Objects.requireNonNullElse(content, "");
		this.style = style == null ? Style.of() : style;
	}

	public static Builder of(String content) {
		return new Builder(content);
	}

	public static Builder bold() {
		return new Builder("").bold();
	}

	public static Builder plain() {
		return new Builder("");
	}

	@Override
	public String render() {
		return style.render(content);
	}

	public static final class Builder {
		private String content;
		private Style style = Style.of();

		private Builder(String content) {
			this.content = content;
		}

		public Builder content(String content) {
			this.content = content;
			return this;
		}

		public Builder bold() {
			this.style = this.style.withBold();
			return this;
		}

		public Builder italic() {
			this.style = this.style.withItalic();
			return this;
		}

		public Builder underline() {
			this.style = this.style.withUnderline();
			return this;
		}

		public Builder foreground(Style.Color color) {
			this.style = this.style.foreground(color);
			return this;
		}

		public Builder background(Style.Color color) {
			this.style = this.style.background(color);
			return this;
		}

		public Text build(String content) {
			return new Text(content, style);
		}

		public Text build() {
			return new Text(content, style);
		}
	}
}


