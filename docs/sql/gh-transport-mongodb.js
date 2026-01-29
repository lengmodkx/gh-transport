// =====================================================
// GH Transport MongoDB Collections
// Database: gh_transport
// =====================================================

use gh_transport;

// 运输轨迹集合
db.createCollection("transport_track", {
    validator: {
        $jsonSchema: {
            bsonType: "object",
            required: ["waybillId", "location", "timestamp"],
            properties: {
                waybillId: { bsonType: "string" },
                dispatchId: { bsonType: "string" },
                vehicleId: { bsonType: "string" },
                location: {
                    bsonType: "object",
                    required: ["lng", "lat"],
                    properties: {
                        lng: { bsonType: "number" },
                        lat: { bsonType: "number" }
                    }
                },
                speed: { bsonType: ["number", "null"] },
                direction: { bsonType: ["number", "null"] },
                accuracy: { bsonType: ["number", "null"] },
                timestamp: { bsonType: "date" },
                createdAt: { bsonType: "date" }
            }
        }
    }
});

// 运输轨迹索引
db.transport_track.createIndex({ "waybillId": 1, "timestamp": -1 });
db.transport_track.createIndex({ "dispatchId": 1 });
db.transport_track.createIndex({ "vehicleId": 1, "timestamp": -1 });

// 运单集合
db.createCollection("waybill", {
    validator: {
        $jsonSchema: {
            bsonType: "object",
            required: ["waybillNo", "dispatchId", "status"],
            properties: {
                waybillNo: { bsonType: "string", unique: true },
                dispatchId: { bsonType: "string" },
                orderId: { bsonType: ["string", "null"] },
                status: { bsonType: "string" },
                origin: {
                    bsonType: "object",
                    properties: {
                        name: { bsonType: "string" },
                        address: { bsonType: "string" },
                        contact: { bsonType: "string" },
                        phone: { bsonType: "string" }
                    }
                },
                destination: {
                    bsonType: "object",
                    properties: {
                        name: { bsonType: "string" },
                        address: { bsonType: "string" },
                        contact: { bsonType: "string" },
                        phone: { bsonType: "string" }
                    }
                },
                currentLocation: {
                    bsonType: ["object", "null"],
                    properties: {
                        lng: { bsonType: "number" },
                        lat: { bsonType: "number" }
                    }
                },
                estimatedArrival: { bsonType: ["date", "null"] },
                actualArrival: { bsonType: ["date", "null"] },
                createdAt: { bsonType: "date" },
                updatedAt: { bsonType: "date" }
            }
        }
    }
});

// 运单索引
db.waybill.createIndex({ "waybillNo": 1 });
db.waybill.createIndex({ "dispatchId": 1 });
db.waybill.createIndex({ "orderId": 1 });
db.waybill.createIndex({ "status": 1 });
