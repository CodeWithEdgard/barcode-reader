package com.br.repository;

import java.util.Optional;
import java.util.Set;
import com.br.domain.Product;

public interface ProductRepository {

    void salvarProduto(Product product);

    Set<Product> listarTodos();

    Optional<Product> encontrarPeloCodigo(String codigo);
}
