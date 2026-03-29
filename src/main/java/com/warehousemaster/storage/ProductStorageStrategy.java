package com.warehousemaster.storage;

import com.warehousemaster.model.AbstractProduct;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public interface ProductStorageStrategy {

    void save(List<AbstractProduct> products, Path path) throws IOException;

    List<AbstractProduct> load(Path path) throws IOException;
}
