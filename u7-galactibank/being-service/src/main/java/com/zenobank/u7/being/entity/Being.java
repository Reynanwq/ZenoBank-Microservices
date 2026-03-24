package com.zenobank.u7.being.entity;

import io.quarkus.mongodb.panache.PanacheMongoEntity;
import io.quarkus.mongodb.panache.common.MongoEntity;
import org.bson.types.ObjectId;

import java.time.LocalDateTime;
import java.util.UUID;

@MongoEntity(collection = "beings")
public class Being extends PanacheMongoEntity {

    public UUID uid;                    // Universal ID - imutável
    public String fullName;             // Nome completo
    public String birthDate;            // Data de nascimento (ISO)
    public UUID originPlanetId;         // Planeta de origem
    public String speciesLabel;         // Espécie (ex: "Saiyajin") - apenas descritivo
    public String status;               // ACTIVE, INACTIVE
    public String meta;                 // JSONB - atributos extras flexíveis
    public LocalDateTime createdAt;     // Data de registro

    // Construtor padrão necessário para o Panache
    public Being() {
        this.createdAt = LocalDateTime.now();
        this.status = "ACTIVE";
    }

    // Construtor para cadastro inicial
    public Being(String fullName, String birthDate, UUID originPlanetId, String speciesLabel) {
        this();
        this.uid = UUID.randomUUID();
        this.fullName = fullName;
        this.birthDate = birthDate;
        this.originPlanetId = originPlanetId;
        this.speciesLabel = speciesLabel;
    }
}