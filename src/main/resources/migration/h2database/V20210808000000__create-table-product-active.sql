create table ProductActive(
    productPk bigint not null,
    productId bigint not null,
    viewCount bigint default 0 not null,
    -- non-functional
    productActivePk identity primary key,
    recordAt timestamp default current_timestamp not null,
    traceId varchar(255)
);

create unique index idx_productPk_on_ProductActive on ProductActive(productPk);
create unique index idx_productId_on_ProductActive on ProductActive(productId);
