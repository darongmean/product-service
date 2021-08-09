create table Product(
    productId bigint auto_increment not null,
    productName varchar(255) not null,
    productPriceUsd decimal(13,4) not null,
    productDescription varchar(5000),
    softDeleteAt timestamp,
    -- non-functional
    productPk identity primary key,
    recordAt timestamp default current_timestamp not null,
    traceId varchar(255)
);

create index idx_productId_on_Product on Product(productId);
