package com.nushi.archiv.client.repository;

import com.nushi.archiv.client.model.ArchivAsset;
import java.util.List;

// Esta classe é responsável por fornecer assets mockados para a interface.
// Por enquanto ela só devolve dados fixos de exemplo.
// No futuro, essa ideia pode evoluir para um repositório real que leia assets importados.
public final class MockAssetRepository {

    // Construtor privado:
    // impede que alguém tente criar um "new MockAssetRepository()".
    // Essa classe vai funcionar só como fornecedora estática de dados.
    private MockAssetRepository() {
    }

    // Metodo estático que devolve todos os assets mockados.
    public static List<ArchivAsset> getAllAssets() {
        return List.of(
                new ArchivAsset("Dark Oak Bed", "Furniture", "1.20.1", 0xFF5B3A1E, 0xFF7A4BD6, 6, true, false),
                new ArchivAsset("Stone Tower", "Structure", "1.20.1", 0xFF4B6E9A, 0xFF2D9CDB, 6, false, false),
                new ArchivAsset("Palm Tree", "Tree", "1.20.1", 0xFF3A8B52, 0xFF2DBE73, 6, true, false),
                new ArchivAsset("Crystal Lamp", "Decoration", "1.20.1", 0xFF7646C7, 0xFF8A5CFF, 7, false, false),
                new ArchivAsset("Mossy Rock Pack", "Terrain", "1.20.1", 0xFF507A42, 0xFF57C784, 8, true, true),
                new ArchivAsset("Cyberpunk Sign", "Prop", "1.20.1", 0xFF8A3BA8, 0xFFDA8A2D, 6, false, false)
        );
    }
}