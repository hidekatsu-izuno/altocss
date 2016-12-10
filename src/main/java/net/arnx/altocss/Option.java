package net.arnx.altocss;

public final class Option<T> {
    public static final Option<Boolean> CONCAT_SOURCE_MAP = Option.of("concat_source_map", Boolean.class);
    public static final Option<String> SOURCE_MAP_SOURCES_ROOT = Option.of("source_map.sources_root", String.class);

    public static <T> Option<T> of(String name, Class<T> type) {
        return new Option<T>(name, type);
    }

    private String name;
    private Class<T> type;

    private Option(String name, Class<T> type) {
        this.name = name;
        this.type = type;
    }

    public String name() {
        return name;
    }

    public Class<T> type() {
        return type;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        Option<?> other = (Option<?>)obj;
        if (name == null) {
            if (other.name != null) return false;
        } else if (!name.equals(other.name)) return false;
        if (type == null) {
            if (other.type != null) return false;
        } else if (!type.equals(other.type)) return false;
        return true;
    }

    @Override
    public String toString() {
        return name;
    }
}
